import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                // kotlin.plugin.compose includes Kotlin Android support in AGP 9.x + Kotlin 2.x
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid()
                configureCompose()
                defaultConfig.targetSdk = 36
            }

            configureKotlinCompiler()
        }
    }
}
