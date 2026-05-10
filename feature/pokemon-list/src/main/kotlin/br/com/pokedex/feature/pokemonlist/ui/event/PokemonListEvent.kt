package br.com.pokedex.feature.pokemonlist.ui.event

sealed interface PokemonListEvent {
    data class NavigateToDetail(val id: Int) : PokemonListEvent
}
