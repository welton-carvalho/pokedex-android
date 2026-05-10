package br.com.pokedex.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val s: Dp = 8.dp,
    val m: Dp = 12.dp,
    val l: Dp = 16.dp,
    val xl: Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val xxxl: Dp = 56.dp,
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }
