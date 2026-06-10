package br.com.pokedex.feature.pokemoncompare.ui.event

sealed interface PokemonCompareEvent {
    data object NavigateBack : PokemonCompareEvent
}
