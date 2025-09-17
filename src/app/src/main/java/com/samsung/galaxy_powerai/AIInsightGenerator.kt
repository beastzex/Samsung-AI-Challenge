package com.samsung.galaxy_powerai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

object AIInsightGenerator {

    /**
     * Generates a detailed and contextual insight for the main dashboard.
     */
    fun getDetailedBatteryInsight(health: String, percentage: Int, isCharging: Boolean, highUsageApp: String): String {
        if (isCharging) {
            return when {
                percentage >= 95 -> "Insight: Nearing full charge. Unplugging soon will help preserve long-term battery health."
                else -> "Insight: Charging normally. For best health, try to keep your charge between 20% and 80%."
            }
        }

        // If not charging, provide insights based on status
        return when {
            percentage < 15 -> "Insight: Battery is critically low. All safety features are now active."
            percentage < 30 -> "Insight: Battery is low. Consider charging soon to stay connected."
            "YouTube" in highUsageApp || "Chrome" in highUsageApp -> "Insight: High drain detected. Web browsing or video streaming is using significant power."
            health == "Overheating" -> "Insight: Your battery is overheating! Please close some apps and let it cool down."
            else -> "Insight: Your battery is operating normally."
        }
    }

    /**
     * NEW: Generates insights on how to preserve long-term battery health.
     */
    fun getHealthPreservationInsight(health: String, temperature: Double): String {
        return when {
            health == "Overheating" || temperature > 40.0 ->
                "AI Insight: Your battery is overheating! Avoid direct sunlight and close heavy apps. High temperatures can permanently damage battery capacity."
            health == "Cold" || temperature < 10.0 ->
                "AI Insight: Your battery is too cold. Performance may be reduced until it warms up. Cold can also affect long-term health."
            else ->
                "AI Insight: To maximize your battery's lifespan, try to keep your charge level between 20% and 80% and avoid extreme temperatures."
        }
    }

    /**
     * Generates a proactive, multi-tiered "Battery Budget" travel plan.
     */
    fun generateBatteryBudgetPlan(tripDurationMinutes: Int, currentBattery: Int, budget: Int): String {
        val navDrain = (tripDurationMinutes * 0.25).toInt() // Navigation is more costly
        val remainingBudget = budget - navDrain

        val plan = StringBuilder("AI Battery Budget Plan:\n")
        plan.append("- Your budget for this trip is: $budget%\n")
        plan.append("- Real-time navigation will use approx: $navDrain%\n")
        plan.append("- Remaining budget for other tasks: $remainingBudget%\n\n")

        if (remainingBudget < 0) {
            plan.append("WARNING: Your budget is TOO LOW for this trip's navigation! You will need at least ${-remainingBudget}% more battery.")
        } else {
            val musicMinutes = (remainingBudget / 0.1).toInt() // Music costs 0.1%/min
            val socialMinutes = (remainingBudget / 0.4).toInt() // Social media costs 0.4%/min
            plan.append("With the remaining $remainingBudget%, you can afford:\n")
            plan.append("- Approx. $musicMinutes minutes of music playback, OR\n")
            plan.append("- Approx. $socialMinutes minutes of social media.\n")
            plan.append("- **AVOID** video streaming to stay within budget.")
        }
        return plan.toString()
    }

    /**
     * This is the HYBRID chatbot function.
     */
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
                "Your battery health is currently reported as 'Good'. For more details and tips, check the 'View Battery Health Details' screen."
            }
            "travel" in lowercasedInput || "navigate" in lowercasedInput -> {
                "I can help with navigation. Please use the 'Travel Guardian' mode from the main screen."
            }
            "hello" in lowercasedInput || "hi" in lowercasedInput -> {
                "Hello! I am PowerAI, your on-device battery assistant. How can I help you?"
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