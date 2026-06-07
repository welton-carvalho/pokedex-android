package br.com.pokedex.feature.pokemoncompare.ui.state

import androidx.compose.runtime.Stable
import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel

@Stable
data class CompareState(
    val isLoading: Boolean = true,
    val first: CompareUiModel? = null,
    val second: CompareUiModel? = null,
    val error: DomainError? = null,
)
