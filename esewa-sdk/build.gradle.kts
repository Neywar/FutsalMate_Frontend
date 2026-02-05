plugins {
    id("com.android.library") version "9.0.0"
    id("org.jetbrains.kotlin.android") version "2.2.10"
}

android {
    namespace = "com.esewa.android.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

