# Source Code for Galaxy PowerAI

Source Code for Galaxy PowerAI
This directory contains the complete source code for the Galaxy PowerAI Android application and the Python script used for training the AI model.

1. Android Application
The root of this directory contains a full, compilable Android Studio project.

To Run:

Open this src folder directly in the latest version of Android Studio.

Allow Gradle to sync and download all the required dependencies.

Before running, ensure you have downloaded the offline map file (Delhi.osm.pbf) and placed it in the app/src/main/assets/maps/ directory as described in the main project README.md.

Build and run the app on an emulator or a physical device.

2. AI Model Training Script
The Google Colab notebook contains the Python script used to synthetically generate the training dataset and train the final advanced_battery_model.tflite.

File: AI_Model_Training.ipynb (or similar name)

To Use: Upload the .ipynb file to Google Colab and run the cells from top to bottom to replicate the data generation and model training process.

3. Key File Locations
For a quick overview of the project's architecture, here are the most important files:

app/src/main/java/.../MainActivity.kt: The main dashboard screen and central controller for the app. Contains the BroadcastReceiver that drives all real-time updates.

app/src/main/java/.../TravelActivity.kt: The offline map and routing screen, powered by osmdroid and GraphHopper.

app/src/main/java/.../ChatActivity.kt: The Virtual Assistant screen, which handles the hybrid online/offline chat logic.

app/src/main/java/.../AIInsightGenerator.kt: The "Expert System" or "Generative Brain" that produces all human-readable text, plans, and insights.

app/src/main/assets/: Contains the final advanced_battery_model.tflite and the offline map data in the /maps sub-folder.
