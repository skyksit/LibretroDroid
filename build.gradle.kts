// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.10")
        // Add hilt-dagger
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.51")
        classpath ("org.jetbrains.kotlin:kotlin-serialization:2.0.10")
    }
}

plugins {
    id("com.android.application") version "8.7.0" apply false
    id("com.android.library") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.10" apply false
    id("com.google.devtools.ksp") version "2.0.10-1.0.24" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
}

tasks.register("clean", Delete::class) {
    @Suppress("DEPRECATION")
    delete(rootProject.buildDir)
}