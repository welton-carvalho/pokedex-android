package br.com.pokedex.feature.pokemondetail.ui.reducer

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemondetail.ui.intent.PokemonDetailIntent
import br.com.pokedex.feature.pokemondetail.ui.model.PokemonDetailUiModel
import br.com.pokedex.feature.pokemondetail.ui.state.PokemonDetailState

object PokemonDetailReducer {

    fun loading(state: PokemonDetailState): PokemonDetailState =
        state.copy(isLoading = true, error = null)

    fun success(state: PokemonDetailState, pokemon: PokemonDetailUiModel): PokemonDetailState =
        state.copy(isLoading = false, pokemon = pokemon, error = null)

    fun error(state: PokemonDetailState, domainError: DomainError): PokemonDetailState =
        state.copy(isLoading = false, error = domainError)

    fun reduce(state: PokemonDetailState, intent: PokemonDetailIntent): PokemonDetailState =
        when (intent) {
            is PokemonDetailIntent.Retry -> loading(state)
            is PokemonDetailIntent.NavigateBack -> state
        }
}
