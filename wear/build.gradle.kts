plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "mx.utng.utngrunner"
    compileSdk = 35

    defaultConfig {
        applicationId = "mx.utng.utngrunner"
        minSdk = 30  // Wear OS 3.0 mínimo
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

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
    // Compose for Wear OS
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.navigation)

    // ViewModel + Lifecycle + Coroutines
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // DataStore (mejor puntuación)
    implementation(libs.androidx.datastore.preferences)

    // Health Services (sensor FC)
    implementation(libs.androidx.health.services.client)

    // Testing
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit)
}
