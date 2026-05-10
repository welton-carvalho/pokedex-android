import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun ApplicationExtension.configureKotlinAndroid() {
    compileSdk = 37
    defaultConfig.minSdk = 26
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

internal fun LibraryExtension.configureKotlinAndroid() {
    compileSdk = 37
    defaultConfig.minSdk = 26
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

internal fun Project.configureKotlinCompiler() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            freeCompilerArgs.addAll(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            )
        }
    }
}

internal fun ApplicationExtension.configureCompose() {
    buildFeatures { compose = true }
}

internal fun LibraryExtension.configureCompose() {
    buildFeatures { compose = true }
}
