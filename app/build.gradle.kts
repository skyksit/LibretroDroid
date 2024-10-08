plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    //Hilt
    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.skyksit.retro"
    compileSdk = 34

    defaultConfig {
        versionCode = 1
        versionName = "1.0"
        applicationId = "com.skyksit.retro"
        minSdk = 28
        targetSdk = 34
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            versionNameSuffix = "-DEBUG"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation (project(":libretrodroid"))
    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation ("androidx.appcompat:appcompat:1.7.0")

    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.github.swordfish90:radialgamepad:2.0.0")
    // timber log
    implementation ("com.jakewharton.timber:timber:5.0.1")
    implementation ("com.google.android.material:material:1.12.0")
    // Dagger Hilt
    implementation ("com.google.dagger:hilt-android:2.51")
    ksp ("com.google.dagger:hilt-compiler:2.51")
    ksp ("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.8.0")
    ksp ("com.google.dagger:hilt-android-compiler:2.51")
}
