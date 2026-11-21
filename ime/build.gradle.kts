plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.spacekeyboard.keyboard.ime"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":designsystem"))
    implementation(project(":feature-symbols"))
    implementation(project(":feature-emoji"))
    implementation(project(":feature-decorate"))
    implementation(project(":feature-clipboard"))
    implementation(project(":feature-translate"))
    implementation(project(":feature-autofix"))
    implementation(project(":feature-images"))
    implementation(libs.androidx.autofill)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.customview)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.savedstate)
    testImplementation(libs.junit)
}
