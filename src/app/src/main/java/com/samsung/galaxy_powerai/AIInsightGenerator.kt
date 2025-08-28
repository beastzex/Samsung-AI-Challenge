package com.samsung.galaxy_powerai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

object AIInsightGenerator {

    fun getBatteryStatusInsight(health: String, percentage: Int): String {
        return when {
            percentage > 80 && health == "Good" -> "Insight: Your battery is in excellent condition. Keep it up!"
            percentage < 20 -> "Insight: Battery is low. Consider charging soon to stay connected."
            else -> "Insight: Your battery is operating normally. You can optimize by managing background apps."
        }
    }

    // --- THIS IS THE UPGRADED FUNCTION ---
    /**
     * Generates a proactive, multi-tiered travel plan based on trip duration and current battery.
     */
    fun generateUsagePlan(tripDurationMinutes: Int, currentBattery: Int, distanceKm: Int): String {
        val estimatedDrain = (tripDurationMinutes * 0.5).toInt() // Simplified: 0.5% drain per minute
        val remainingBattery = currentBattery - estimatedDrain
        // We define a "safe arrival" as having at least 10% battery left.
        val buffer = remainingBattery - 10

        val plan = StringBuilder("AI Travel Plan ($distanceKm km):\n")
        plan.append("- Estimated travel time: $tripDurationMinutes minutes.\n")
        plan.append("- You will arrive with approximately $remainingBattery% battery.\n\n")

        // Provide tiered recommendations based on the buffer battery
        when {
            buffer > 20 -> { // Safe tier
                plan.append("Recommendation: Your battery is sufficient. For optimal performance, you can enable Battery Saver mode.")
            }
            buffer in 5..20 -> { // Caution tier
                plan.append("Recommendation: Your battery is sufficient, but it's best to be careful. To guarantee your arrival, I recommend:\n- Lowering screen brightness.\n- Turning off Bluetooth if not in use.")
            }
            else -> { // Warning tier
                plan.append("CRITICAL: Battery is very low for this trip! To ensure you arrive safely, you MUST:\n- Lower brightness to minimum.\n- Turn off Bluetooth & Wi-Fi.\n- Close all other apps.")
            }
        }
        return plan.toString()
    }

    suspend fun getChatbotResponse(inputText: String, batteryPercentage: Int, prediction: String): String {
        val lowercasedInput = inputText.toLowerCase(Locale.ROOT)

        return when {
            "how long" in lowercasedInput && "battery" in lowercasedInput -> {
                "Based on your current usage, the AI predicts your battery will last for approximately $prediction."
            }
            "optimize" in lowercasedInput || "save battery" in lowercasedInput -> {
                "I can help with that. The 'Optimize Apps' button on the main screen will take you to your phone's battery settings."
            }
            "health" in lowercasedInput && "battery" in lowercasedInput -> {
                "Your battery health is currently reported as 'Good'."
            }
            "travel" in lowercasedInput || "navigate" in lowercasedInput -> {
                "I can help with navigation. Use the 'Travel Guardian' mode from the main screen for battery-efficient routing."
            }
            "hello" in lowercasedInput || "hi" in lowercasedInput -> {
                "Hello! I am Galaxy PowerAI, your on-device battery assistant. How can I help you?"
            }
            "battery" in lowercasedInput || "status" in lowercasedInput -> {
                "Your battery is currently at $batteryPercentage. The AI predicts it will last for approximately $prediction."
            }
            else -> {
                getGeneralKnowledgeAnswer()
            }
        }
    }

    private suspend fun getGeneralKnowledgeAnswer(): String {
        return withContext(Dispatchers.IO) {
            try {
                val jsonText = URL("https://uselessfacts.js.org/api/v2/facts/random").readText()
                val jsonObject = JSONObject(jsonText)
                "Here's a random fact for you: ${jsonObject.getString("text")}"
            } catch (e: Exception) {
                "I can only answer questions about the battery when I'm offline."
            }
        }
    }
}