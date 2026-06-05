package br.com.pokedex.feature.pokemoncompare.ui.intent

sealed interface CompareIntent {
    data class RetryPokemon(val side: Side) : CompareIntent
}
