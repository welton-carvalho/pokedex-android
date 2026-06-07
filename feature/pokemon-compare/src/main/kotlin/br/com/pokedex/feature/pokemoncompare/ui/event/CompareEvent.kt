package br.com.pokedex.feature.pokemoncompare.ui.event

sealed interface CompareEvent {
    data object NavigateBack : CompareEvent
}
