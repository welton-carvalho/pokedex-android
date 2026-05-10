plugins {
    alias(libs.plugins.pokedexlab.android.library)
}

android {
    namespace = "br.com.pokedex.data.repository"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":data:network"))
    implementation(project(":data:local"))
    implementation(libs.androidx.paging.runtime)
    implementation(libs.koin.android)
}
