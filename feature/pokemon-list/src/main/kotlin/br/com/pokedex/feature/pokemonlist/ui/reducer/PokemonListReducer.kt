package br.com.pokedex.feature.pokemonlist.ui.reducer

import br.com.pokedex.feature.pokemonlist.ui.intent.PokemonListIntent
import br.com.pokedex.feature.pokemonlist.ui.state.PokemonListState

private const val MAX_COMPARE_SELECTION = 2

object PokemonListReducer {
    fun reduce(state: PokemonListState, intent: PokemonListIntent): PokemonListState =
        when (intent) {
            is PokemonListIntent.Retry -> state.copy(isLoading = true, error = null)
            is PokemonListIntent.ClickPokemon -> state
            is PokemonListIntent.ToggleCompareMode -> {
                val nextCompareMode = !state.isCompareMode
                state.copy(
                    isCompareMode = nextCompareMode,
                    selectedForCompare = if (nextCompareMode) state.selectedForCompare else emptySet(),
                )
            }
            is PokemonListIntent.ToggleSelectForCompare -> reduceToggleSelect(state, intent.id)
            is PokemonListIntent.ResetCompareMode -> state.copy(
                isCompareMode = false,
                selectedForCompare = emptySet(),
            )
        }

    private fun reduceToggleSelect(state: PokemonListState, id: Int): PokemonListState {
        if (!state.isCompareMode) return state
        val current = state.selectedForCompare
        return when {
            id in current -> state.copy(selectedForCompare = current - id)
            current.size < MAX_COMPARE_SELECTION -> state.copy(selectedForCompare = current + id)
            else -> state
        }
    }
}
