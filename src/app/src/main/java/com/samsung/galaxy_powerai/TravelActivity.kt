package com.samsung.galaxy_powerai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.util.*
import android.util.Log

class TravelActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var tvTravelPlan: TextView
    private lateinit var etDestination: EditText
    private lateinit var etBatteryBudget: EditText
    private lateinit var btnFindRoute: Button
    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = false
    private var currentRoute: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        mapView = findViewById(R.id.map_view)
        tvTravelPlan = findViewById(R.id.tvTravelPlan)
        etDestination = findViewById(R.id.etDestination)
        etBatteryBudget = findViewById(R.id.etBatteryBudget)
        btnFindRoute = findViewById(R.id.btnFindRoute)

        initializeTts()
        setupMapView()

        btnFindRoute.setOnClickListener {
            val destination = etDestination.text.toString()
            val budget = etBatteryBudget.text.toString().toIntOrNull()
            if (destination.isNotBlank() && budget != null) {
                calculateRouteToDestination(destination, budget)
            } else {
                Toast.makeText(this, "Please enter a destination and budget", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupMapView() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        val mapController = mapView.controller
        mapController.setZoom(12.5)
        mapController.setCenter(GeoPoint(28.6139, 77.2090)) // Default to New Delhi
    }

    private fun initializeTts() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.US)
                isTtsInitialized = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    private fun calculateRouteToDestination(destinationAddress: String, budget: Int) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { startLocation: Location? ->
            if (startLocation == null) {
                Toast.makeText(this, "Could not get current location.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(this@TravelActivity, Locale.getDefault())
                    val endAddresses = geocoder.getFromLocationName(destinationAddress, 1)

                    if (endAddresses.isNullOrEmpty()) {
                        withContext(Dispatchers.Main) { Toast.makeText(this@TravelActivity, "Destination not found.", Toast.LENGTH_SHORT).show() }
                        return@launch
                    }

                    val startPoint = GeoPoint(startLocation.latitude, startLocation.longitude)
                    val endPoint = GeoPoint(endAddresses[0].latitude, endAddresses[0].longitude)

                    // --- NETWORK CALL TO OPENROUTESERVICE ---
                    val client = OkHttpClient()
                    val apiKey = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjYwYWZiNDg1MTBhOTQxZjc5Y2I0NzBlMWIzMjRiYTUxIiwiaCI6Im11cm11cjY0In0=" // PASTE YOUR KEY HERE
                    val url = "https://api.openrouteservice.org/v2/directions/driving-car" +
                            "?api_key=$apiKey" +
                            "&start=${startPoint.longitude},${startPoint.latitude}" +
                            "&end=${endPoint.longitude},${endPoint.latitude}"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()
                    // ------------------------------------------

                    if (response.isSuccessful && responseBody != null) {
                        // --- Parse the JSON Response (CORRECTED) ---
                        val json = JSONObject(responseBody)
                        val features = json.getJSONArray("features")
                        val properties = features.getJSONObject(0).getJSONObject("properties")
                        // The "summary" is an object INSIDE the "properties" object
                        val summary = properties.getJSONObject("summary") // <-- Corrected
                        val durationSeconds = summary.getDouble("duration")
                        val distanceMeters = summary.getDouble("distance")
                        // Get the route coordinates from the "geometry"
                        val coordinates = features.getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates")

                        val travelTimeMinutes = (durationSeconds / 60).toInt()
                        val distanceKm = (distanceMeters / 1000).toInt()
                        val currentBattery = 63 // Placeholder

                        val plan = AIInsightGenerator.generateBatteryBudgetPlan(travelTimeMinutes, currentBattery, budget)

                        val routePoints = mutableListOf<GeoPoint>()
                        for (i in 0 until coordinates.length()) {
                            val coord = coordinates.getJSONArray(i)
                            routePoints.add(GeoPoint(coord.getDouble(1), coord.getDouble(0)))
                        }

                        withContext(Dispatchers.Main) {
                            drawRoute(routePoints)
                            tvTravelPlan.text = plan
                            tvTravelPlan.visibility = View.VISIBLE
                            speak(plan)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TravelActivity", "Route lookup failed", e)
                    withContext(Dispatchers.Main) { Toast.makeText(this@TravelActivity, "Route lookup failed. Check internet.", Toast.LENGTH_SHORT).show() }
                }
            }
        }
    }

    private fun drawRoute(routePoints: List<GeoPoint>) {
        if (currentRoute != null) {
            mapView.overlays.remove(currentRoute)
        }
        val polyline = Polyline()
        polyline.color = Color.BLUE
        polyline.width = 12.0f
        polyline.setPoints(routePoints)
        mapView.overlays.add(polyline)
        mapView.invalidate()
        currentRoute = polyline
    }

    private fun speak(text: String) {
        if (isTtsInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}