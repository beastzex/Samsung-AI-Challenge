# Technical Documenatation of the Project

## 1. Project Architecture & Methodology

1.1. High-Level Architecture

Galaxy PowerAI is a native Android application developed in Kotlin. The core architecture is designed around a Hybrid On-Device AI System that runs entirely on the edge, ensuring user privacy, offline functionality, and real-time performance. This system consists of two main components: a Predictive Brain for numerical forecasting and a Generative Brain for producing human-readable insights and responses. These AI components are served by a real-time data pipeline powered by native Android system services.

1.2. Development Methodology

Our approach was an iterative, prototype-driven methodology suited for a fast-paced hackathon environment. We focused on building and testing features in modular components (MainActivity, TravelActivity, etc.). We employed a "fail-fast" debugging strategy using Android Studio's Logcat to rapidly identify and resolve runtime errors. A key strategic decision was to use a synthetically generated dataset for the AI model, which allowed for rapid prototyping and the creation of a high-quality, controlled dataset to prove the viability of our multivariate prediction model within the hackathon's timeframe.

## 2. Tech Stack and Development Environment

<img width="817" height="596" alt="image" src="https://github.com/user-attachments/assets/852ed54d-1420-4485-a23a-00c4a7066992" />

## 3. The AI Engine: In-Depth Implementation

Our Hybrid AI System is composed of two distinct brains that work together.

3.1. The Predictive Brain (Multivariate Dense Model)
This is the forecasting engine that analyzes device data to predict battery drain.

<img width="818" height="468" alt="image" src="https://github.com/user-attachments/assets/2e2a8c68-f458-4b30-b9bd-ba63ddd91a45" />

3.2. The Generative Brain (Expert System)

This component acts as our practical, on-device "Generative AI" engine.

Implementation: A Kotlin object (Singleton pattern) named AIInsightGenerator.kt.

Functionality: It uses rule-based logic (when statements) to translate data into human-readable text. It is responsible for generating:

Contextual Insights: e.g., "Your battery is in excellent condition."

Proactive Travel Plans: A multi-line recommendation based on trip duration vs. battery level.

Hybrid Chatbot Responses: It handles on-device queries about the battery. For general knowledge questions, it makes a network call to a free, public API (uselessfacts.js.org), demonstrating a practical hybrid AI approach.

## 4. Core Application Feature Implementation

Real-time Monitoring & Automation (BroadcastReceiver): The app's nervous system is a BroadcastReceiver that listens for the system's ACTION_BATTERY_CHANGED intent. This event-driven component is highly efficient and triggers all key logic: UI updates, safety alerts (SMS, Emergency Mode), and the collection of data for the AI prediction engine.

Offline Mapping & Routing (TravelActivity):

The GraphHopper engine is initialized with a local Delhi.osm.pbf map file. To ensure a smooth user experience, the heavy task of loading this map data is offloaded to a background thread using kotlin.concurrent.thread, preventing UI freezes (ANRs).

The UI is rendered using an osmdroid MapView.

Address-to-coordinate conversion (Geocoding) is handled by Android's native Geocoder, which requires a one-time internet connection per search.

Data Persistence (SharedPreferences): The EmergencyContactActivity uses Android's SharedPreferences to securely and persistently store the user's chosen emergency contact number, ensuring it's always available for the automated SMS alert.

Voice Interaction (TextToSpeech): The app uses the native Android TTS engine to provide offline, voice-enabled insights and travel plans, creating a richer, more accessible Human-AI Interaction.

UI/UX: The user interface is built with modern Android components, including CardView for a clean, elevated look and RecyclerView for displaying the dynamic list of messages in the chat interface. The design is dark-themed and high-contrast for readability and reduced power consumption on OLED screens.

## 5. User Guide

User Guide:
Welcome to Galaxy PowerAI! Here’s a quick guide to using your intelligent battery guardian.

  1. The Main Dashboard
When you open the app, you are greeted with your main dashboard. This screen gives you an at-a-glance view of your phone's status:

Live Battery Status: See your current battery percentage and the AI's real-time prediction of how long it will last.

AI Insight: Below the prediction, the AI provides a smart, contextual insight about your battery's health, which is also spoken aloud.

High Usage Apps: See a list of the apps that have been using the most power in the last hour.

Quick Actions: Use the buttons at the bottom to access the app's powerful features.

  2. Using the Travel Guardian
This is your intelligent, offline navigation tool.

Tap the "Travel Guardian Mode" button.

The map screen will open. Wait a moment for the toast message "Map ready!" to appear as the offline map loads in the background.

In the search box at the top, type your destination (e.g., "Qutub Minar, Delhi") and tap "Find Route".

The app will find your current location, calculate the best offline route, and draw it on the map. You will then see and hear the AI's custom Travel Plan, advising you on your battery usage for the trip.

  3. Chatting with the Virtual Assistant
You can ask your on-device AI assistant questions.

Tap the "Virtual Assistant" button.

In the chat screen, type your question in the message box and tap "Send".

Try asking different types of questions:

On-Device Query: Ask, "What is my battery health?" or "How long will my battery last?" for an instant, offline answer.

Hybrid Query: Ask, "Tell me a fact" or any other general knowledge question to see the hybrid assistant fetch information from the internet.

  4. Setting Up Your Emergency Contact
This is a critical safety feature that you should set up first.

From the main dashboard, tap the "Emergency Contacts" button.

On the new screen, enter the phone number of a trusted contact.

Tap "Save Contact".

This number will now automatically receive an SMS with your last known location if your phone's battery drops to a critical level.

  5. Automatic Safety Features
Two features work automatically in the background to protect you:

Emergency SMS: When your battery drops to 5% and is not charging, the app automatically sends the alert to your saved emergency contact.

Emergency Reserve Mode: If the battery drops even further to 3%, the app will automatically launch a minimalist, high-contrast screen with only essential functions (Phone, Messages, Maps) to preserve the last bit of power.

## 6. Installation Instructions 

To build and run this project from the source code, please follow these steps.

1. Prerequisites

Android Studio (latest stable version recommended)

An Android Emulator or physical device running API 26 (Oreo) or higher.

2. Clone the Repository
   
Open your terminal and clone this GitHub repository:

 // use command 
 git clone [Your Repository URL]

3. Open in Android Studio

Launch Android Studio.

Select "Open an existing project" and navigate to the folder you just cloned.

Allow Android Studio to sync the Gradle dependencies. This may take a few minutes.

4. Download the Offline Map File
   
The offline routing feature requires a map data file.

Download the Delhi.osm.pbf file from this direct link: https://download.bbbike.org/osm/bbbike/Delhi/Delhi.osm.pbf

In the Android Studio Project panel, navigate to app/src/main/assets/.

Create a new directory inside assets and name it maps.

Place the downloaded Delhi.osm.pbf file inside this new app/src/main/assets/maps/ directory.

5. Build and Run

Once the Gradle sync is complete and the map file is in place, you can run the app.

Select your target device from the dropdown menu at the top.

Click the green "Run 'app'" (▶️) button.
