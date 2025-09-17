package com.samsung.galaxy_powerai

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BatteryDetailsActivity : AppCompatActivity() {

    private lateinit var tvHealthStatus: TextView
    private lateinit var tvBatteryTech: TextView
    private lateinit var tvBatteryTemp: TextView
    private lateinit var tvBatteryVoltage: TextView
    private lateinit var tvHealthInsight: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battery_details)

        tvHealthStatus = findViewById(R.id.tvHealthStatus)
        tvBatteryTech = findViewById(R.id.tvBatteryTech)
        tvBatteryTemp = findViewById(R.id.tvBatteryTemp)
        tvBatteryVoltage = findViewById(R.id.tvBatteryVoltage)
        tvHealthInsight = findViewById(R.id.tvHealthInsight)
    }

    private val batteryDetailsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                // Get Health Status
                val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val health = when (healthInt) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    else -> "Unknown"
                }
                tvHealthStatus.text = "Health Status: $health"

                // Get Battery Technology
                val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
                tvBatteryTech.text = "Technology: $technology"

                // Get Temperature
                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10.0
                tvBatteryTemp.text = "Temperature: $temp °C"

                // Get Voltage
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0
                tvBatteryVoltage.text = "Voltage: $voltage V"

                // Get AI Insight for Health
                val insight = AIInsightGenerator.getHealthPreservationInsight(health, temp)
                tvHealthInsight.text = insight
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryDetailsReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryDetailsReceiver)
    }
}