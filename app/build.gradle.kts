plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.appdraw"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appdraw"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    
    // Shimmer Effect
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ZegoCloud Live Streaming UIKit
    implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_live_streaming_android:+")
}

tasks.register("testClasses") {
    dependsOn(tasks.matching { it.name.endsWith("UnitTestClasses") })
}
