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

