import org.gradle.kotlin.dsl.androidTestAnnotationProcessor
import org.gradle.kotlin.dsl.androidTestImplementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.androidx.room)
    // kotlinx serialization
    kotlin("plugin.serialization") version "2.4.0"
    id("com.google.devtools.ksp")
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.modloader.bm3"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }



    defaultConfig {
        applicationId = "com.modloader.bm3"
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.serialization.json)
    // coil libs
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // retrofit
    implementation(libs.retrofit)

    // room DB
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    annotationProcessor(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)

    // hilt DI
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.lifecycle.viewmodel)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}