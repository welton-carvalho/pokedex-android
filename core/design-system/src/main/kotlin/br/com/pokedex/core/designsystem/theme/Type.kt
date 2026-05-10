package br.com.pokedex.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Poppins via Google Fonts downloadable font (configured in app module manifest)
// Falls back to system SansSerif until font is downloaded
val PoppinsFontFamily = FontFamily.SansSerif

val HeadlineBold = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
)

val Subtitle1Bold = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 16.sp,
)

val Subtitle2Bold = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val Subtitle3Bold = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    lineHeight = 16.sp,
)

val Body1Regular = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 16.sp,
)

val Body2Regular = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
)

val Body3Regular = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    lineHeight = 16.sp,
)

val CaptionRegular = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 8.sp,
    lineHeight = 12.sp,
)

val PokedexTypography = Typography(
    displayLarge = HeadlineBold,
    titleLarge = HeadlineBold,
    titleMedium = Subtitle1Bold,
    titleSmall = Subtitle2Bold,
    bodyLarge = Body1Regular,
    bodyMedium = Body2Regular,
    bodySmall = Body3Regular,
    labelSmall = CaptionRegular,
)
