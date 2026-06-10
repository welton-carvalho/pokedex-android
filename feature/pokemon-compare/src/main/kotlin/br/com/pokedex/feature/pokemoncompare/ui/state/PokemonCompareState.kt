package br.com.pokedex.feature.pokemoncompare.ui.state

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel

enum class CompareSide { FIRST, SECOND }

@Immutable
sealed interface SideUiState {
    data object Loading : SideUiState
    data class Success(val pokemon: ComparePokemonUiModel) : SideUiState
    data class Error(val error: DomainError) : SideUiState
}

@Stable
data class PokemonCompareState(
    val first: SideUiState = SideUiState.Loading,
    val second: SideUiState = SideUiState.Loading,
)
