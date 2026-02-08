plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.politicalevents"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.politicalevents"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // -------------------------
    // Core Android
    // -------------------------
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

        implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")


    // -------------------------
    // Compose (ONE BOM ONLY)
    // -------------------------
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // -------------------------
    // Material 3
    // -------------------------
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // -------------------------
    // Navigation
    // -------------------------
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // -------------------------
    // ViewModel
    // -------------------------
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    // -------------------------
    // Firebase (BOM FIRST)
    // -------------------------
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firestore (Admin whitelist)
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // -------------------------
    // Coroutines (Firebase await)
    // -------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")

    // -------------------------
    // Networking (Optional, keep if used)
    // -------------------------
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // -------------------------
    // Testing
    // -------------------------
    testImplementation("junit:junit:4.13.2")
}
