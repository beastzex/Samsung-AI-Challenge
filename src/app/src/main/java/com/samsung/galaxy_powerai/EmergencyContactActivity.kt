package com.samsung.galaxy_powerai

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EmergencyContactActivity : AppCompatActivity() {

    private lateinit var etContactNumber: EditText
    private lateinit var btnSaveContact: Button
    private lateinit var tvCurrentContact: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency_contact)

        etContactNumber = findViewById(R.id.etContactNumber)
        btnSaveContact = findViewById(R.id.btnSaveContact)
        tvCurrentContact = findViewById(R.id.tvCurrentContact)

        // When the screen opens, load and display any previously saved contact
        loadContact()

        // When the save button is clicked...
        btnSaveContact.setOnClickListener {
            val number = etContactNumber.text.toString()
            if (number.isNotBlank()) {
                saveContact(number)
            } else {
                Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveContact(number: String) {
        // Access the app's private storage
        val sharedPrefs = getSharedPreferences("PowerAIPrefs", Context.MODE_PRIVATE)
        // Save the number with the key "EMERGENCY_CONTACT"
        with(sharedPrefs.edit()) {
            putString("EMERGENCY_CONTACT", number)
            apply() // Save the changes
        }
        Toast.makeText(this, "Emergency contact saved!", Toast.LENGTH_SHORT).show()
        // Update the display with the new number
        loadContact()
        etContactNumber.text.clear()
    }

    private fun loadContact() {
        val sharedPrefs = getSharedPreferences("PowerAIPrefs", Context.MODE_PRIVATE)
        // Load the saved number. If none exists, it will show "None".
        val currentNumber = sharedPrefs.getString("EMERGENCY_CONTACT", "None")
        tvCurrentContact.text = "Currently saved: $currentNumber"
    }
}