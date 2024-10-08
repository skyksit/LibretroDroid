plugins {
    id ("com.android.library")
    id ("kotlin-android")
    id ("maven-publish")
}

android {
    namespace = "com.swordfish.libretrodroid"
    compileSdk = 34

    defaultConfig {
        minSdk = 28

        consumerProguardFiles("consumer-rules.pro")

        externalNativeBuild {
            cmake {
                // Available arguments are inside ${SDK}/cmake/.../android.toolchain.cmake file
                arguments("-DANDROID_STL=c++_static")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        cmake {
            version = "3.22.1"
            path = file("src/main/cpp/CMakeLists.txt")
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
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.20")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
}

afterEvaluate {
    publishing {
        publications {
            create("maven-public", MavenPublication::class) {
                groupId = "com.github.skyksit"
                artifactId = "libretrodroid"
                version = "0.10.0"
                from(components["release"])
            }
        }
    }
}