package br.com.pokedex.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val PokedexColorScheme = lightColorScheme(
    primary = PokedexRed,
    onPrimary = White,
    background = GrayBackground,
    onBackground = Gray1,
    surface = White,
    onSurface = Gray1,
    surfaceVariant = GrayBackground,
    onSurfaceVariant = Gray2,
    outline = Gray3,
)

@Composable
fun PokedexLabTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = PokedexColorScheme,
            typography = PokedexTypography,
            shapes = PokedexShapes,
            content = content,
        )
    }
}

object PokedexTheme {
    val spacing: Spacing
        @Composable get() = LocalSpacing.current
}
