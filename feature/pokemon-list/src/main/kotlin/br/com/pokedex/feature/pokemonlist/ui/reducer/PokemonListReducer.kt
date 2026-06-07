package br.com.pokedex.feature.pokemonlist.ui.reducer

import br.com.pokedex.feature.pokemonlist.ui.intent.PokemonListIntent
import br.com.pokedex.feature.pokemonlist.ui.state.PokemonListState

object PokemonListReducer {
    fun reduce(state: PokemonListState, intent: PokemonListIntent): PokemonListState =
        when (intent) {
            is PokemonListIntent.Retry -> state.copy(isLoading = true, error = null)
            is PokemonListIntent.ClickPokemon -> state
            is PokemonListIntent.ToggleCompareMode ->
                state.copy(isCompareMode = !state.isCompareMode, selectedIds = emptyList())
            is PokemonListIntent.ToggleSelection -> {
                val current = state.selectedIds
                val updated = when {
                    current.contains(intent.id) -> current - intent.id
                    current.size < 2 -> current + intent.id
                    else -> current
                }
                state.copy(selectedIds = updated)
            }
        }
}
