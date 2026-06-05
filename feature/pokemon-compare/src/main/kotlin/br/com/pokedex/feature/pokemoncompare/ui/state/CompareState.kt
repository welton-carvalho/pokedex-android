package br.com.pokedex.feature.pokemoncompare.ui.state

import androidx.compose.runtime.Stable
import br.com.pokedex.core.common.result.AsyncResult
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel

@Stable
data class CompareState(
    val isInvalidInput: Boolean = false,
    val pokemonA: AsyncResult<CompareUiModel> = AsyncResult.Loading,
    val pokemonB: AsyncResult<CompareUiModel> = AsyncResult.Loading,
)
