plugins {
    alias(libs.plugins.pokedexlab.android.application)
}

android {
    namespace = "br.com.pokedex"
    buildFeatures { buildConfig = true }
    defaultConfig {
        applicationId = "br.com.pokedex"
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
}

// objectbox-android-objectbrowser includes objectbox-android — exclude to avoid duplicate classes in debug
configurations.configureEach {
    if (name == "debugRuntimeClasspath") {
        exclude(group = "io.objectbox", module = "objectbox-android")
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin.compose)
    implementation(libs.timber)
    implementation(libs.bundles.coil)

    implementation(project(":core:common"))
    implementation(project(":core:design-system"))
    implementation(project(":core:domain"))
    implementation(project(":core:route:navigation"))
    implementation(project(":core:observability"))
    implementation(project(":feature:pokemon-list"))
    implementation(project(":feature:pokemon-detail"))
    implementation(project(":feature:pokemon-compare"))
    implementation(project(":data:network"))
    implementation(project(":data:local"))
    releaseImplementation(libs.objectbox.android)
    debugImplementation(libs.objectbox.android.objectbrowser)
    implementation(project(":data:repository"))

    debugImplementation(libs.bundles.compose.debug)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.bundles.compose.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
}
