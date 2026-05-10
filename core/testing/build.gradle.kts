plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.pokedexlab.android.compose)
}

android {
    namespace = "br.com.pokedex.core.testing"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.test)
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.bundles.junit5)
    implementation(libs.junit5.engine)
    implementation(libs.mockk.android)
    implementation(libs.mockk.agent)
    implementation(libs.koin.test)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.testing)
}
