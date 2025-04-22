import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.group18.gosell"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.group18.gosell"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        val placesApiKey = localProperties.getProperty("PLACES_API_KEY") ?: ""
        val cloudinaryCloudName = localProperties.getProperty("CLOUDINARY_CLOUD_NAME") ?: ""
        val cloudinaryApiKey = localProperties.getProperty("CLOUDINARY_API_KEY") ?: ""
        val cloudinaryApiSecret = localProperties.getProperty("CLOUDINARY_API_SECRET") ?: ""

        buildConfigField("String", "PLACES_API_KEY", "\"$placesApiKey\"")
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"$cloudinaryApiKey\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$cloudinaryApiSecret\"")

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
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.coil.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.cloudinary.android)
    implementation(libs.okhttp)
    implementation(libs.play.services.location)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.play.services.location)
    implementation(libs.places)
}