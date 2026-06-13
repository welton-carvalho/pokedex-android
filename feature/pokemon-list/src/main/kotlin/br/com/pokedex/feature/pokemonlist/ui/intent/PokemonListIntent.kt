package br.com.pokedex.feature.pokemonlist.ui.intent

sealed interface PokemonListIntent {
    data object Retry : PokemonListIntent
    data class ClickPokemon(val id: Int) : PokemonListIntent
}
