package br.com.pokedex.feature.pokemondetail.ui.event

sealed interface PokemonDetailEvent {
    data object NavigateBack : PokemonDetailEvent
}
