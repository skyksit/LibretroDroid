// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath (libs.kotlin.gradle)
        classpath (libs.google.services)
        classpath (libs.firebase.crashlytics.gradle)
        // Add hilt-dagger
        classpath (libs.dagger.hilt.android.gradle)
        classpath (libs.kotlin.serialization)
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.devtoolsKsp) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
}

tasks.register("clean", Delete::class) {
    @Suppress("DEPRECATION")
    delete(rootProject.buildDir)
}