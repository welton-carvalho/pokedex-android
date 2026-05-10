package br.com.pokedex.feature.pokemonlist.ui.state

import br.com.pokedex.core.common.result.DomainError

data class PokemonListState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
)
