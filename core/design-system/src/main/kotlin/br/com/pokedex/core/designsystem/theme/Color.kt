package br.com.pokedex.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Identity
val PokedexRed = Color(0xFFDC0A2D)

// Grayscale
val Gray1 = Color(0xFF212121)
val Gray2 = Color(0xFF666666)
val Gray3 = Color(0xFFE0E0E0)
val GrayBackground = Color(0xFFEFEFEF)
val White = Color(0xFFFFFFFF)

// Pokémon Type Colors
val TypeBug = Color(0xFFA7B723)
val TypeDark = Color(0xFF75574C)
val TypeDragon = Color(0xFF7037FF)
val TypeElectric = Color(0xFFF9CF30)
val TypeFairy = Color(0xFFE69EAC)
val TypeFighting = Color(0xFFC12239)
val TypeFire = Color(0xFFF57D31)
val TypeFlying = Color(0xFFA891EC)
val TypeGhost = Color(0xFF70559B)
val TypeGrass = Color(0xFF74CB48)
val TypeGround = Color(0xFFDEC16B)
val TypeIce = Color(0xFF9AD6DF)
val TypeNormal = Color(0xFFAAA67F)
val TypePoison = Color(0xFFA43E9E)
val TypePsychic = Color(0xFFFB5584)
val TypeRock = Color(0xFFB69E31)
val TypeSteel = Color(0xFFB7B9D0)
val TypeWater = Color(0xFF6493EB)

fun pokemonTypeColor(typeName: String): Color = when (typeName.lowercase()) {
    "bug" -> TypeBug
    "dark" -> TypeDark
    "dragon" -> TypeDragon
    "electric" -> TypeElectric
    "fairy" -> TypeFairy
    "fighting" -> TypeFighting
    "fire" -> TypeFire
    "flying" -> TypeFlying
    "ghost" -> TypeGhost
    "grass" -> TypeGrass
    "ground" -> TypeGround
    "ice" -> TypeIce
    "normal" -> TypeNormal
    "poison" -> TypePoison
    "psychic" -> TypePsychic
    "rock" -> TypeRock
    "steel" -> TypeSteel
    "water" -> TypeWater
    else -> Gray2
}
