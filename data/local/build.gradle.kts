plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.objectbox)
}

android {
    namespace = "br.com.pokedex.data.local"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.objectbox.android)
    implementation(libs.objectbox.kotlin)
    implementation(libs.koin.android)
}
