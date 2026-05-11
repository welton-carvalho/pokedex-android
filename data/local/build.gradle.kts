plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.objectbox)
}

android {
    namespace = "br.com.pokedex.data.local"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.objectbox.android)
    implementation(libs.objectbox.kotlin)
    kapt(libs.objectbox.processor)
    implementation(libs.koin.android)
}
