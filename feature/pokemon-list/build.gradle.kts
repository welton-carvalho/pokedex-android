plugins {
    alias(libs.plugins.pokedexlab.android.feature)
}

android {
    namespace = "br.com.pokedex.feature.pokemonlist"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin.compose)
    implementation(libs.bundles.navigation3)
    implementation(libs.bundles.paging)
    implementation(libs.bundles.coil)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.compose.material.icons)

    implementation(project(":core:route:keys"))
    implementation(project(":core:common"))
    implementation(project(":core:design-system"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
    implementation(project(":data:repository"))

    debugImplementation(libs.bundles.compose.debug)

    testImplementation(project(":core:testing"))
    testImplementation(libs.bundles.junit5)
    testImplementation(libs.mockk.android)
    testImplementation(libs.mockk.agent)
    testImplementation(libs.kotlinx.coroutines.test)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
}
