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
import com.google.android.gms.location.LocationServices
import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.config.Profile
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.util.Locale
import kotlin.concurrent.thread

class TravelActivity : AppCompatActivity() {

    // --- UI and Map Properties ---
    private lateinit var mapView: MapView
    private lateinit var tvTravelPlan: TextView
    private lateinit var etDestination: EditText
    private lateinit var btnFindRoute: Button

    // --- AI and Voice Properties ---
    private lateinit var tts: TextToSpeech
    private var isTtsInitialized = false

    // --- Routing Engine Properties ---
    private var hopper: GraphHopper? = null
    private var isRoutingEngineReady = false
    private var currentRoute: Polyline? = null

    // --- NEW: Define the valid bounds for our Delhi map ---
    private val delhiBounds = com.graphhopper.util.shapes.BBox(76.8, 77.4, 28.4, 28.9) // MinLon, MaxLon, MinLat, MaxLat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel)

        Configuration.getInstance().load(applicationContext, getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        mapView = findViewById(R.id.map)
        tvTravelPlan = findViewById(R.id.tvTravelPlan)
        etDestination = findViewById(R.id.etDestination)
        btnFindRoute = findViewById(R.id.btnFindRoute)

        initializeTts()
        setupMapView()
        initializeGraphHopper()

        btnFindRoute.isEnabled = false // Disable button until engine is ready
        btnFindRoute.setOnClickListener {
            val destination = etDestination.text.toString()
            if (destination.isNotBlank()) {
                calculateRouteToDestination(destination)
            } else {
                Toast.makeText(this, "Please enter a destination", Toast.LENGTH_SHORT).show()
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

    private fun initializeGraphHopper() {
        Toast.makeText(this, "Loading offline map data...", Toast.LENGTH_SHORT).show()
        thread(start = true) {
            try {
                val ghDir = File(filesDir, "graphhopper")
                if (!ghDir.exists()) ghDir.mkdir()
                val mapFile = File(ghDir, "NewDelhi.osm.pbf")
                if (!mapFile.exists()) {
                    assets.open("maps/NewDelhi.osm.pbf").use { input ->
                        mapFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                hopper = GraphHopper().apply {
                    setOSMFile(mapFile.absolutePath)
                    setGraphHopperLocation(ghDir.absolutePath)
                    setProfiles(Profile("car").setVehicle("car").setWeighting("fastest"))
                    importOrLoad()
                }
                isRoutingEngineReady = true

                runOnUiThread {
                    Toast.makeText(this, "Map ready! Calculating demo route.", Toast.LENGTH_SHORT).show()
                    btnFindRoute.isEnabled = true // Enable the button now
                    calculateDemoRoute() // Automatically run the demo route
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error loading map: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Function to calculate a hardcoded route to demonstrate the feature works
    private fun calculateDemoRoute() {
        val start = GeoPoint(28.6129, 77.2295) // India Gate
        val end = GeoPoint(28.6562, 77.2410)   // Red Fort
        calculateRoute(start, end)
    }

    // This new function handles the entire flow for a user-defined destination
    // This NEW function hardcodes the start location to prevent emulator GPS bugs
    private fun calculateRouteToDestination(destinationAddress: String) {
        if (!isRoutingEngineReady || hopper == null) {
            Toast.makeText(this, "Routing engine is not ready yet.", Toast.LENGTH_SHORT).show()
            return
        }

        // --- THE FIX ---
        // We will now use a reliable, hardcoded start point inside Delhi
        val startPoint = GeoPoint(28.6129, 77.2295) // India Gate

        // The rest of the logic runs on a background thread
        thread {
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                // Step 1: Convert the destination address to coordinates (Geocoding)
                val addresses = geocoder.getFromLocationName(destinationAddress, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val endAddress = addresses[0]
                    val endPoint = GeoPoint(endAddress.latitude, endAddress.longitude)

                    // Step 2: Calculate the route
                    val request = GHRequest(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude)
                        .setProfile("car")
                    val response = hopper!!.route(request)

                    // Step 3: Display the result on the main thread
                    runOnUiThread {
                        if (response.hasErrors()) {
                            Toast.makeText(this, response.errors.first().message, Toast.LENGTH_LONG).show()
                        } else {
                            val path = response.best
                            drawRoute(path)

                            val travelTimeMinutes = (path.time / 1000 / 60).toInt()
                            val distanceKm = (path.distance / 1000).toInt()
                            val currentBattery = 63 // Placeholder
                            val plan = AIInsightGenerator.generateUsagePlan(travelTimeMinutes, currentBattery, distanceKm)

                            tvTravelPlan.text = plan
                            tvTravelPlan.visibility = View.VISIBLE
                            speak(plan)
                        }
                    }
                } else {
                    runOnUiThread { Toast.makeText(this, "Destination not found.", Toast.LENGTH_SHORT).show() }
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Address lookup failed. Check internet.", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    // A single, reusable function to calculate and display any route
    private fun calculateRoute(startPoint: GeoPoint, endPoint: GeoPoint) {
        if (!isRoutingEngineReady || hopper == null) return

        thread { // Run routing on a background thread
            val request = GHRequest(startPoint.latitude, startPoint.longitude, endPoint.latitude, endPoint.longitude)
                .setProfile("car")
            val response = hopper!!.route(request)

            runOnUiThread {
                if (response.hasErrors()) {
                    Toast.makeText(this, response.errors.first().message, Toast.LENGTH_LONG).show()
                } else {
                    val path = response.best
                    drawRoute(path)

                    val travelTimeMinutes = (path.time / 1000 / 60).toInt()
                    val distanceKm = (path.distance / 1000).toInt()
                    val currentBattery = 63 // Placeholder
                    val plan = AIInsightGenerator.generateUsagePlan(travelTimeMinutes, currentBattery, distanceKm)

                    tvTravelPlan.text = plan
                    tvTravelPlan.visibility = View.VISIBLE
                    speak(plan)
                }
            }
        }
    }

    private fun drawRoute(path: com.graphhopper.ResponsePath) {
        if (currentRoute != null) {
            mapView.overlays.remove(currentRoute)
        }
        val polyline = Polyline()
        polyline.color = Color.BLUE
        polyline.width = 12.0f
        path.points.forEach { geoPoint ->
            polyline.addPoint(GeoPoint(geoPoint.lat, geoPoint.lon))
        }
        mapView.overlays.add(polyline)
        mapView.invalidate()
        currentRoute = polyline
    }

    private fun speak(text: String) {
        if (isTtsInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    // --- Lifecycle Management ---
    override fun onResume() { super.onResume() ; mapView.onResume() }
    override fun onPause() { super.onPause() ; mapView.onPause() }
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        hopper?.close()
        super.onDestroy()
    }
}