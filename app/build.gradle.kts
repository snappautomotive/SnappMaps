import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.snappautomotive.maps"
    compileSdk {
        version = release(libs.versions.android.sdk.compile.get().toInt())
    }
    buildToolsVersion = libs.versions.android.build.tools.get()

    defaultConfig {
        applicationId = "com.snappautomotive.maps"
        versionCode = 10102
        versionName = "1.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")

        minSdk {
            version = release(libs.versions.android.sdk.min.get().toInt())
        }
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

    signingConfigs {
        getByName("debug").apply {
            storeFile = file("../keys/firmware-dev-key.jks")
            storePassword = "snappautomotive"
            keyAlias = "SnappMappsFirmwareDevKey"
            keyPassword = "snappautomotive"
        }
    }

    compileOptions.apply {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.window)
    implementation(libs.google.material)
    implementation(libs.osmdroid)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.test.junit)
}