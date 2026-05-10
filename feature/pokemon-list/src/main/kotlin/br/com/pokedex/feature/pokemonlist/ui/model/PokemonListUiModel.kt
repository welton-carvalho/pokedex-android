package br.com.pokedex.feature.pokemonlist.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class PokemonListUiModel(
    val id: Int,
    val displayName: String,
    val imageUrl: String,
)
