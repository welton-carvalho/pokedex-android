package br.com.pokedex.feature.pokemoncompare.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class StatUiModel(
    val label: String,
    val value: Int,
    val maxValue: Int = 255,
)
