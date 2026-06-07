package br.com.pokedex.feature.pokemoncompare.ui.reducer

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.intent.CompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareState

object CompareReducer {

    fun loading(state: CompareState): CompareState =
        state.copy(isLoading = true, error = null)

    fun success(state: CompareState, first: CompareUiModel, second: CompareUiModel): CompareState =
        state.copy(isLoading = false, first = first, second = second, error = null)

    fun error(state: CompareState, domainError: DomainError): CompareState =
        state.copy(isLoading = false, error = domainError)

    fun reduce(state: CompareState, intent: CompareIntent): CompareState =
        when (intent) {
            is CompareIntent.Retry -> loading(state)
            is CompareIntent.NavigateBack -> state
        }
}
