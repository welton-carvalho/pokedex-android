plugins {
    alias(libs.plugins.pokedexlab.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "br.com.pokedex.data.network"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:observability"))
    implementation(libs.bundles.retrofit)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.koin.android)
    debugImplementation(libs.chucker.library)
    releaseImplementation(libs.chucker.library.no.op)
}
