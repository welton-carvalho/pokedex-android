package br.com.pokedex.feature.pokemondetail.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class PokemonDetailUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val typeColor: Color,
    val types: List<String>,
    val height: String,
    val weight: String,
    val abilities: List<String>,
    val stats: List<StatUiModel>,
    val description: String,
)

@Immutable
data class StatUiModel(
    val label: String,
    val value: Int,
)
