package br.com.pokedex.feature.pokemoncompare.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CompareUiModel(
    val id: Int,
    val name: String,
    val formattedNumber: String,
    val imageUrl: String,
    val types: List<String>,
    val primaryTypeColor: Color,
    val heightMeters: Float,
    val weightKg: Float,
    val stats: List<StatUiModel>,
)
