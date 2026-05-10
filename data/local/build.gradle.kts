plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.objectbox)
    alias(libs.plugins.ksp)
}

android {
    namespace = "br.com.pokedex.data.local"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.objectbox.android)
    implementation(libs.objectbox.kotlin)
    ksp(libs.objectbox.processor)
    implementation(libs.koin.android)
}
