package br.com.pokedex.feature.pokemonlist.ui.reducer

import br.com.pokedex.feature.pokemonlist.ui.intent.PokemonListIntent
import br.com.pokedex.feature.pokemonlist.ui.state.PokemonListState
import br.com.pokedex.feature.pokemonlist.ui.state.PokemonListState.Companion.MAX_COMPARE_SELECTION

object PokemonListReducer {
    fun reduce(state: PokemonListState, intent: PokemonListIntent): PokemonListState =
        when (intent) {
            is PokemonListIntent.Retry -> state.copy(isLoading = true, error = null)
            is PokemonListIntent.ClickPokemon -> state
            is PokemonListIntent.ToggleSelectionMode -> {
                val enabling = !state.isSelectionMode
                state.copy(
                    isSelectionMode = enabling,
                    selectedIds = if (enabling) state.selectedIds else emptyList(),
                )
            }
            is PokemonListIntent.ToggleSelection -> when {
                intent.id in state.selectedIds ->
                    state.copy(selectedIds = state.selectedIds - intent.id)
                state.selectedIds.size < MAX_COMPARE_SELECTION ->
                    state.copy(selectedIds = state.selectedIds + intent.id)
                else -> state // limite atingido; ViewModel emite ShowSelectionLimit
            }
            // Navegação + limpeza pós-navegação são tratadas no ViewModel (Q4=A)
            is PokemonListIntent.ClickCompare -> state
        }
}
