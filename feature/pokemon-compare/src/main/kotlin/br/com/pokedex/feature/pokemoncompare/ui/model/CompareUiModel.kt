package br.com.pokedex.feature.pokemoncompare.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class CompareUiModel(
    val id: Int,
    val name: String,
    val number: String,
    val imageUrl: String,
    val typeColor: Color,
    val types: List<String>,
    val stats: List<CompareStatUiModel>,
)

@Immutable
data class CompareStatUiModel(
    val label: String,
    val value: Int,
)

@Immutable
data class StatComparisonRow(
    val label: String,
    val firstValue: Int,
    val secondValue: Int,
    val firstWins: Boolean,
    val secondWins: Boolean,
)
