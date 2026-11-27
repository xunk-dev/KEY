plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.spacekeyboard.keyboard"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.spacekeyboard.keyboard"
        minSdk = 24
        targetSdk = 37
        versionCode = 8
        versionName = "0.7.0"
    }

    signingConfigs {
        create("release") {
            val ks = file("release.keystore")
            if (ks.exists()) {
                storeFile = ks
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
                keyAlias = System.getenv("KEY_ALIAS") ?: "spacekeyboard"
                keyPassword = System.getenv("KEY_PASSWORD") ?: ""
            }
        }
    }

    splits {
        abi {
            isEnable = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) && !it.contains("bundle", ignoreCase = true) }
            reset()
            include("arm64-v8a")
            isUniversalApk = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val ks = file("release.keystore")
            if (ks.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    androidResources {
        localeFilters += listOf("en", "ar")
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
            excludes += "META-INF/*.version"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
            excludes += "DebugProbesKt.bin"
            excludes += "kotlin-tooling-metadata.json"
        }
    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":ime"))
    implementation(project(":designsystem"))
    implementation(project(":feature-emoji"))
    implementation(project(":feature-decorate"))
    implementation(project(":feature-clipboard"))
    implementation(project(":feature-translate"))
    implementation(project(":feature-autofix"))
    implementation(project(":feature-images"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling.preview)
    testImplementation(libs.junit)
}
