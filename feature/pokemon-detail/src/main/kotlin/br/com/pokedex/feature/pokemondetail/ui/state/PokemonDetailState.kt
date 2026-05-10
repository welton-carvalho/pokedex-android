package br.com.pokedex.feature.pokemondetail.ui.state

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemondetail.ui.model.PokemonDetailUiModel

data class PokemonDetailState(
    val isLoading: Boolean = true,
    val pokemon: PokemonDetailUiModel? = null,
    val error: DomainError? = null,
)
