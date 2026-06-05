package br.com.pokedex.feature.pokemonlist.ui.state

import androidx.compose.runtime.Stable
import br.com.pokedex.core.common.result.DomainError

@Stable
data class PokemonListState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    val isCompareMode: Boolean = false,
    val selectedForCompare: Set<Int> = emptySet(),
)
