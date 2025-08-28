package com.samsung.galaxy_powerai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EmergencyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)

        val btnCall: Button = findViewById(R.id.btnCall)
        val btnMessages: Button = findViewById(R.id.btnMessages)
        val btnMaps: Button = findViewById(R.id.btnMaps)
        val tvEmergencyBatteryLevel: TextView = findViewById(R.id.tvEmergencyBatteryLevel)

        val batteryLevel = intent.getIntExtra("BATTERY_LEVEL", 0)
        tvEmergencyBatteryLevel.text = "$batteryLevel% Remaining"

        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            startActivity(intent)
        }

        btnMessages.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_MESSAGING)
            startActivity(intent)
        }

        btnMaps.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            }
        }
    }
}