package br.com.pokedex.feature.pokemoncompare.ui.intent

sealed interface CompareIntent {
    data object Retry : CompareIntent
    data object NavigateBack : CompareIntent
}
