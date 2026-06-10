package br.com.pokedex.feature.pokemoncompare.ui.intent

import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide

sealed interface PokemonCompareIntent {
    data class Retry(val side: CompareSide) : PokemonCompareIntent
    data object NavigateBack : PokemonCompareIntent
}
