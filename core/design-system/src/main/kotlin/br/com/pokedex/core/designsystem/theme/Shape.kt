package br.com.pokedex.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PokedexShapes = Shapes(
    extraSmall = RoundedCornerShape(7.dp),   // name tag on card
    small = RoundedCornerShape(8.dp),         // cards, content areas
    medium = RoundedCornerShape(12.dp),       // sort card
    large = RoundedCornerShape(16.dp),        // search bar, buttons
    extraLarge = RoundedCornerShape(20.dp),   // bottom sheet header
)

val TypeChipShape = RoundedCornerShape(10.dp)
