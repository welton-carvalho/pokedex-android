package br.com.pokedex.feature.pokemoncompare.ui.reducer

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.intent.PokemonCompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.PokemonCompareState
import br.com.pokedex.feature.pokemoncompare.ui.state.SideUiState

object PokemonCompareReducer {

    fun loading(state: PokemonCompareState, side: CompareSide): PokemonCompareState =
        state.setSide(side, SideUiState.Loading)

    fun success(
        state: PokemonCompareState,
        side: CompareSide,
        pokemon: ComparePokemonUiModel,
    ): PokemonCompareState = state.setSide(side, SideUiState.Success(pokemon))

    fun error(
        state: PokemonCompareState,
        side: CompareSide,
        domainError: DomainError,
    ): PokemonCompareState = state.setSide(side, SideUiState.Error(domainError))

    fun reduce(state: PokemonCompareState, intent: PokemonCompareIntent): PokemonCompareState =
        when (intent) {
            is PokemonCompareIntent.Retry -> loading(state, intent.side)
            is PokemonCompareIntent.NavigateBack -> state
        }

    private fun PokemonCompareState.setSide(side: CompareSide, value: SideUiState) =
        when (side) {
            CompareSide.FIRST -> copy(first = value)
            CompareSide.SECOND -> copy(second = value)
        }
}
