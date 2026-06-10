package br.com.pokedex.feature.pokemoncompare.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ComparePokemonUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val typeColor: Color,
    val types: List<String>,
    val height: String,
    val weight: String,
    val abilities: List<String>,
    val description: String,
    val stats: List<CompareStatUiModel>,
)

@Immutable
data class CompareStatUiModel(
    val label: String,
    val value: Int,
)

enum class StatWinner { FIRST, SECOND, TIE }

@Immutable
data class StatComparisonRow(
    val label: String,
    val firstValue: Int,
    val secondValue: Int,
    val winner: StatWinner,
)
