package br.com.pokedex.feature.pokemonlist.ui.event

sealed interface PokemonListEvent {
    data class NavigateToDetail(val id: Int) : PokemonListEvent
    data class NavigateToCompare(val idA: Int, val idB: Int) : PokemonListEvent
    data object ShowSelectionLimitReached : PokemonListEvent
}
