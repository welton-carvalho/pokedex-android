package br.com.pokedex.feature.pokemonlist.ui.event

sealed interface PokemonListEvent {
    data class NavigateToDetail(val id: Int) : PokemonListEvent
    data class NavigateToCompare(val firstId: Int, val secondId: Int) : PokemonListEvent
}
