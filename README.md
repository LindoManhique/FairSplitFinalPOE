FairSplit

FairSplit is a personal expense management and group cost-splitting Android application developed as part of the Final Year POE (Portfolio of Evidence) for the Diploma in Information Technology (Software Development) at Rosebank College. The app helps users track expenses, manage group contributions, and convert currencies in real time using modern Android technologies.
Project Details

Institution: Rosebank College
Module: Work Integrated Learning (WIL)
Project Type: Final POE Submission
Student Developer: Lindo Manhique

Overview

FairSplit allows users to register, log in, and manage their shared or personal expenses through a simple and user-friendly interface. The app integrates Firebase for authentication and database storage, supporting both online and offline modes for seamless usability. It is optimized for Android 8.0 (API 26) and above.

Features

User Authentication: Secure login and registration using Firebase Authentication.

Expense Management: Create, view, and delete expenses with individual or group tracking.

Group Management: Create and manage groups for shared expenses.

Currency Conversion: Real-time exchange rate fetching for accurate expense comparison.

Offline Mode: Toggle offline mode for demos or limited connectivity environments.

Language Support: English and Afrikaans localization.

Modern UI: Built with Material Design, ConstraintLayout, and ViewBinding.

Data Security: Cloud Firestore with secure user-based access.

Technology Stack
Component	Technology
Language	Kotlin
Architecture	MVVM (Model–View–ViewModel)
UI Toolkit	AndroidX, Material Design
Database	Firebase Firestore
Authentication	Firebase Auth
Networking	Retrofit2, Gson
Async Processing	Kotlin Coroutines
Build Tool	Gradle (KTS)
IDE	Android Studio Ladybug or later

How to Run the App

Clone the repository

git clone https://github.com/LindoManhique/FairSplitFinalPOE.git


Open in Android Studio

File → Open → Select the FairSplit folder.

Allow Gradle to sync.

Add Google Services

Place your Firebase google-services.json inside the app/ directory.

Build the project

Click Build > Make Project or use:

./gradlew assembleDebug


Run on an emulator or Android device

Minimum SDK: 24 (Android 7.0)

Target SDK: 34 (Android 14)

Sign in or Register

Use the built-in registration screen to create a new Firebase account.

Create a Group

Add a new group and invite or simulate members.

Add Expenses

Add demo or real expenses.

Test Currency Conversion

Fetch exchange rates using the built-in “Fetch ZAR → USD” button.

Toggle Offline Mode

Use the switch at the top of the Groups screen to simulate offline data.

How to Build a Signed APK or AAB

In Android Studio:

Go to Build → Generate Signed Bundle / APK...

Choose Android App Bundle or APK.

Select your keystore file and credentials.

Build Type: release

Output file:

.aab → app/build/outputs/bundle/release/app-release.aab

.apk → app/build/outputs/apk/release/app-release.apk

For Firebase Auth to work on release builds:

Add the SHA-1 key from your keystore to Firebase Project Settings.

Firebase Setup Guide

Create a Firebase project at https://console.firebase.google.com
.

Register the app package name:

com.example.fairsplit


Enable Authentication → Email/Password.

Enable Cloud Firestore and set rules:

rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}


Download the google-services.json file and place it in the app/ directory.

Evidence of Publication Preparation

As part of the POE submission requirements:

Signed release APK and AAB files have been generated.

Screenshots of the running app on device are provided (Groups, Expenses, Settings screens).

Google Play Console upload evidence (Internal Testing release) is included.

License

This project is submitted as part of an academic requirement and may not be used for commercial purposes without permission from the developer.

below is Screenshotof signaAPK
<img width="1936" height="994" alt="SignAPK" src="https://github.com/user-attachments/assets/96013710-60ca-4684-9893-6d381f2b1d87" />


Youtube Link : https://youtu.be/0c77TUscU3s

Repo Link: https://github.com/LindoManhique/FairSplitFinalPOE



