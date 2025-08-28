# Galaxy PowerAI
A proactive, on-device AI guardian for Android that eliminates battery anxiety. Features predictive battery life, offline navigation, and automated safety alerts. A submission for the Samsung EnnovateX 2025 AI Challenge.


# Samsung EnnovateX 2025 AI Challenge Submission

- **Problem Statement** - *PS : 6 , "Crafting the Next Generation of Human-AI Interaction on the Edge" *
- **Team name** - *Detroit*  **Order ID - #15974784**
- **Team members (Names)** - *Akshat Arya(Team Leader)*, *Aarvi*, *Ketki Gogia*, *Abhishek Yadav* 
- **Demo Video Link** - *(Upload the Demo video on Youtube as a public or unlisted video and share the link. Google Drive uploads or any other uploads are not allowed.)*

# Key Features
Hybrid On-Device AI: A powerful combination of a predictive TensorFlow Lite model and a generative "Expert System" that runs 100% offline.

Predictive Battery Forecasting: Accurately predicts remaining battery life in hours and minutes based on real-time usage.

Voice-Enabled AI Insights: Delivers smart, contextual, and spoken advice about your battery's health and status.

Interactive Offline Travel Guardian: A fully offline map and routing system that generates AI-powered battery usage plans to ensure you reach your destination safely.

Complete Automated Safety Net:

Automatically sends an Emergency SMS with your location to a trusted contact when the battery is critical.

Automatically switches to a minimalist Emergency Reserve Mode at very low battery levels.

Simulated Virtual Assistant: A hybrid chatbot that answers battery-related questions offline and general knowledge questions online.


# Project Artefacts

Ideation documnent is present in docs folder

Technical Documentation: All technical details for this project are docs folder

Source Code: The complete source code is located in the /src directory of this repository.

Model Used: The predictive model is advanced_battery_model.tflite, located in the /assets folder. It was custom-trained for this project.

Dataset Used: The model was trained on a synthetically generated dataset that mimics real-world usage patterns. The Python script used to generate this data is available in the samsung_ai.ipynb file in /src folder.

# Overview of Technical Implementation
Our prototype is a native Android application built using Kotlin.

AI Engine: The core of our project is a Hybrid AI System.

The Predictive Brain is a Multivariate Time-Series Forecasting Model using a Dense Neural Network, built with TensorFlow and deployed with TensorFlow Lite.

The Generative Brain is a Kotlin-based Expert System (AIInsightGenerator.kt) that uses rule-based logic to generate human-readable text and voice output.

Real-time Monitoring: The app's nervous system is a BroadcastReceiver that listens for the system's ACTION_BATTERY_CHANGED intent, allowing the app to react instantly to any change in battery state.

Offline Mapping & Routing: The Travel Guardian feature is powered by GraphHopper (offline routing) and osmdroid (offline maps). The heavy task of loading the map data is offloaded to a background thread to keep the UI smooth and responsive.

Data Persistence: The app uses Android's SharedPreferences to securely store the user's chosen emergency contact number.

Voice Interaction: The app uses the native Android TextToSpeech (TTS) engine to provide offline, voice-enabled insights.

# Our Journey: Building Galaxy PowerAI
We've all felt it: that sinking feeling when you're out, navigating a new city, and you see your phone's battery hit 10%. In that moment, your powerful smartphone becomes a source of anxiety. That's the problem we set out to solve. We didn't want to just build another battery saver; we wanted to create an intelligent guardian that works with you, keeping you safe and connected when it matters most.

Our Philosophy: An AI That You Can Trust
From day one, we committed to two core principles that guided every technical decision:

On-Device First: In an age of constant cloud connectivity, we made a deliberate choice. Our guardian had to work anywhere, anytime, with or without an internet connection. This meant all of our AI processing had to happen on the edge. This approach guarantees 100% user privacy and makes the app incredibly fast and reliable.

A Hybrid AI Approach: We knew a single AI model wasn't enough. A true assistant needs to be both a brilliant mathematician and a great communicator. So, we built a Hybrid AI System with two distinct brains.

Meet the Brains of the Operation
The Forecaster (Our Predictive Brain): At the heart of our app is a custom-built TensorFlow Lite model. We taught it to be a master forecaster. It doesn't just look at the battery percentage; it analyzes a live sequence of four different data points—Battery Level, Screen Brightness, Network Status, and Charging State—to understand the context of how you're using your phone and predict what will happen next.

The Communicator (Our Generative Brain): Numbers are meaningless without good communication. That's why we built a "Generative Brain" right into the app with Kotlin, which we call our Expert System (AIInsightGenerator.kt). This is the AI's voice and personality. It takes the complex predictions from the Forecaster and translates them into simple, helpful advice, proactive travel plans, and friendly chat responses.
