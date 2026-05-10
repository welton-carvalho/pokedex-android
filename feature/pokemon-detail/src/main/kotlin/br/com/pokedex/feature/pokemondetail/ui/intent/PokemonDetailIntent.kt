package br.com.pokedex.feature.pokemondetail.ui.intent

sealed interface PokemonDetailIntent {
    data object Retry : PokemonDetailIntent
    data object NavigateBack : PokemonDetailIntent
}
