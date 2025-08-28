package com.samsung.galaxy_powerai

// All import statements must be at the top of the file

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // --- UI Properties ---
    private lateinit var tvBatteryLevel: TextView
    private lateinit var tvTimePrediction: TextView
    private lateinit var tvAppUsage: TextView
    private lateinit var tvInsight: TextView
    private lateinit var btnOptimize: Button
    private lateinit var btnTravelMode: Button
    private lateinit var btnChat: Button
    private lateinit var btnEmergencyContacts: Button

    // --- AI Model Properties ---
    private lateinit var tflite: Interpreter
    private val tfliteModel: MappedByteBuffer by lazy { loadModelFile() }
    private val batteryDataHistory = mutableListOf<Float>()
    private val HISTORY_SIZE = 10
    private var isCharging = false

    // --- Voice Engine Properties ---
    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = false

    // --- Safety Feature Properties ---
    private var emergencySmsSent = false
    private var emergencyModeLaunched = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvBatteryLevel = findViewById(R.id.tvBatteryLevel)
        tvTimePrediction = findViewById(R.id.tvTimePrediction)
        tvAppUsage = findViewById(R.id.tvAppUsage)
        tvInsight = findViewById(R.id.tvInsight)
        btnOptimize = findViewById(R.id.btnOptimize)
        btnTravelMode = findViewById(R.id.btnTravelMode)
        btnChat = findViewById(R.id.btnChat)
        btnEmergencyContacts = findViewById(R.id.btnEmergencyContacts)

        btnOptimize.setOnClickListener { openSamsungBatterySettings() }
        btnTravelMode.setOnClickListener { startActivity(Intent(this, TravelActivity::class.java)) }
        btnChat.setOnClickListener {
            val currentBatteryText = tvBatteryLevel.text.toString()
            val currentPredictionText = tvTimePrediction.text.toString()
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("CURRENT_BATTERY", currentBatteryText)
            intent.putExtra("CURRENT_PREDICTION", currentPredictionText)
            startActivity(intent)
        }
        btnEmergencyContacts.setOnClickListener {
            startActivity(Intent(this, EmergencyContactActivity::class.java))
        }

        tflite = Interpreter(tfliteModel)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                isTtsInitialized = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }

        requestAppPermissions()
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level * 100 / scale.toFloat()
                tvBatteryLevel.text = "${batteryPct.toInt()}%"

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

                val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val health = when (healthInt) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                    else -> "OK"
                }
                val insight = AIInsightGenerator.getBatteryStatusInsight(health, batteryPct.toInt())
                tvInsight.text = insight
                speak(insight)

                if (batteryPct <= 5.0f && !isCharging && !emergencySmsSent) {
                    val prefs = context?.getSharedPreferences("PowerAIPrefs", Context.MODE_PRIVATE)
                    val emergencyContact = prefs?.getString("EMERGENCY_CONTACT", null)
                    if (emergencyContact != null) {
                        val message = "Alert: My phone battery is critically low and may turn off soon."
                        sendEmergencySms(emergencyContact, message)
                        emergencySmsSent = true
                    }
                }

                if (batteryPct <= 3.0f && !isCharging && !emergencyModeLaunched) {
                    val emergencyIntent = Intent(context, EmergencyActivity::class.java)
                    emergencyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    emergencyIntent.putExtra("BATTERY_LEVEL", batteryPct.toInt())
                    context?.startActivity(emergencyIntent)
                    emergencyModeLaunched = true
                }

                if (batteryPct > 10.0f) {
                    emergencySmsSent = false
                    emergencyModeLaunched = false
                }

                if (batteryDataHistory.isEmpty() || batteryDataHistory.last() != batteryPct) {
                    batteryDataHistory.add(batteryPct)
                }
                while (batteryDataHistory.size > HISTORY_SIZE) {
                    batteryDataHistory.removeAt(0)
                }
                if (batteryDataHistory.size == HISTORY_SIZE) {
                    runPrediction()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        checkForUsageStatsPermission()
        displayMostUsedApp()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }

    private fun speak(text: String) {
        if (isTtsInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    private fun requestAppPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION),
            101
        )
    }

    private fun sendEmergencySms(phoneNumber: String, message: String) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                var locationString = "Location not available."
                if (location != null) {
                    locationString = "Last known location: geo:${location.latitude},${location.longitude}"
                }
                val fullMessage = "$message\n$locationString"
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, fullMessage, null, null)
            }
        } catch (e: SecurityException) {
            // Location permission not granted
        }
    }

    private fun openSamsungBatterySettings() {
        try {
            // This is the specific intent to open Samsung's battery optimization screen
            val intent = Intent()
            intent.setClassName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
            startActivity(intent)
        } catch (e: Exception) {
            // A highly reliable fallback: open this app's own "App Info" screen in settings.
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

            // --- THIS IS THE CORRECTED LINE ---
            // We specify the full path to Android's Uri class to remove the ambiguity
            val uri = android.net.Uri.fromParts("package", packageName, null)
            // ------------------------------------

            intent.data = uri
            startActivity(intent)
        }
    }

    private fun checkForUsageStatsPermission() {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    private fun displayMostUsedApp() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.HOUR_OF_DAY, -1)
        val startTime = calendar.timeInMillis
        val stats =
            usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

        if (stats?.isNotEmpty() == true) {
            val topApps = stats.toList().sortedByDescending { it.totalTimeInForeground }.take(3)
            val appNames = StringBuilder()
            topApps.forEach { usageStats ->
                if (usageStats.totalTimeInForeground > 0) {
                    try {
                        val appName = packageManager.getApplicationLabel(
                            packageManager.getApplicationInfo(
                                usageStats.packageName,
                                PackageManager.GET_META_DATA
                            )
                        )
                        if (appNames.isNotEmpty()) appNames.append(", ")
                        appNames.append(appName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        // Ignore
                    }
                }
            }

            if (appNames.isNotEmpty()) {
                tvAppUsage.text = "Last Hour: $appNames"
            } else {
                tvAppUsage.text = "No recent app usage"
            }
        }
    }

    private fun getCurrentBrightness(): Int {
        return try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            128
        }
    }

    private fun getNetworkStatus(): Int {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return when {
            activeNetwork == null -> 0
            activeNetwork.type == ConnectivityManager.TYPE_WIFI -> 1
            activeNetwork.type == ConnectivityManager.TYPE_MOBILE -> 2
            else -> 0
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd("advanced_battery_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun runPrediction() {
        if (batteryDataHistory.size < HISTORY_SIZE) return

        val currentBrightness = getCurrentBrightness().toFloat()
        val currentNetwork = getNetworkStatus().toFloat()
        val currentChargingStatus = if (isCharging) 1.0f else 0.0f
        val normBrightness = currentBrightness / 255.0f
        val normNetwork = currentNetwork / 2.0f

        val inputBuffer = ByteBuffer.allocateDirect(1 * HISTORY_SIZE * 4 * 4).apply {
            order(ByteOrder.nativeOrder())
            for (i in 0 until HISTORY_SIZE) {
                putFloat(batteryDataHistory[i] / 100.0f)
                putFloat(normBrightness)
                putFloat(normNetwork)
                putFloat(currentChargingStatus)
            }
        }

        val outputBuffer = ByteBuffer.allocateDirect(1 * 1 * 4).apply {
            order(ByteOrder.nativeOrder())
        }
        tflite.run(inputBuffer, outputBuffer)

        outputBuffer.rewind()
        val predictedValue = outputBuffer.float * 100.0f
        val lastKnownLevel = batteryDataHistory.last()
        var dischargeRate = lastKnownLevel - predictedValue

        if (!isCharging && dischargeRate > 0 && dischargeRate < 0.1) {
            dischargeRate = 0.1f
        }

        if (isCharging) {
            tvTimePrediction.text = "AI Prediction: Charging"
        } else if (dischargeRate > 0.001) {
            val minutesRemaining = (lastKnownLevel / dischargeRate).toInt()
            val hours = minutesRemaining / 60
            val minutes = minutesRemaining % 60
            tvTimePrediction.text = "AI Prediction: ${hours}h ${minutes}m"
        } else {
            tvTimePrediction.text = "AI Prediction: Stable"
        }
    }
}