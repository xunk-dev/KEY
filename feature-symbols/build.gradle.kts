plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.spacekeyboard.keyboard.symbols"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":designsystem"))
    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
}
