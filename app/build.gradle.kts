plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fairsplit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fairsplit"
        minSdk = 24
        targetSdk = 34

        // Bump these for public releases
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Only package English & Afrikaans
        resConfigs("en", "af")
    }

    buildFeatures { viewBinding = true }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // shrinkResources = true
        }
        debug {
            // IMPORTANT: no package suffix so it matches google-services.json
            // applicationIdSuffix = ""   // (or just leave it out entirely)
            // You can keep a name suffix if you want:
            versionNameSuffix = "-debug"
            // minify stays false by default
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // AndroidX / UI
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.10.0") // safe
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Google Sign-In (exclude credentials to avoid its AF strings)
    implementation("com.google.android.gms:play-services-auth:21.1.1") {
        exclude(group = "androidx.credentials", module = "credentials")
        exclude(group = "androidx.credentials", module = "credentials-play-services-auth")
    }

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.24")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// Never let Credentials sneak in via transitive deps
configurations.all {
    exclude(group = "androidx.credentials", module = "credentials")
    exclude(group = "androidx.credentials", module = "credentials-play-services-auth")
}

// Keep Material pinned
configurations.configureEach {
    resolutionStrategy {
        force("com.google.android.material:material:1.10.0")
    }
}
