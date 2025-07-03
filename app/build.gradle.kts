plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.movieswipe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.movieswipe"
        minSdk = 31
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Hilt for DI
    implementation("com.google.dagger:hilt-android:2.51.1")
//    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // Firebase Cloud Messaging (for push notifications)
    implementation("com.google.firebase:firebase-messaging:24.1.0")
    implementation("com.google.firebase:firebase-analytics:22.0.0")
}

// Hilt plugin
apply(plugin = "dagger.hilt.android.plugin")

kapt {
    correctErrorTypes = true
}