package br.com.pokedex.feature.pokemonlist.ui.state

import androidx.compose.runtime.Stable
import br.com.pokedex.core.common.result.DomainError

@Stable
data class PokemonListState(
    val isLoading: Boolean = false,
    val error: DomainError? = null,
    val isSelectionMode: Boolean = false,
    val selectedIds: List<Int> = emptyList(),
) {
    val canCompare: Boolean get() = selectedIds.size == MAX_COMPARE_SELECTION

    companion object {
        const val MAX_COMPARE_SELECTION = 2
    }
}
