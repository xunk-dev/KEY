plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

fun setting(name: String): String =
    providers.environmentVariable(name).orElse(providers.gradleProperty(name)).orElse("").get().replace("\"", "").trim()

fun quoted(name: String): String = "\"${setting(name)}\""

android {
    namespace = "com.spacekeyboard.keyboard.translate"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "TRANSLATE_URL", quoted("TRANSLATE_URL"))
        buildConfigField("String", "FIREBASE_APP_ID", quoted("FIREBASE_APP_ID"))
        buildConfigField("String", "FIREBASE_API_KEY", quoted("FIREBASE_API_KEY"))
        buildConfigField("String", "FIREBASE_PROJECT_ID", quoted("FIREBASE_PROJECT_ID"))
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
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":designsystem"))
    implementation(project(":feature-emoji"))
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    implementation(libs.mlkit.translate)
    implementation(libs.mlkit.language.id)
    testImplementation(libs.junit)
}
