package br.com.pokedex.feature.pokemoncompare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.feature.pokemoncompare.mapper.toCompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.event.PokemonCompareEvent
import br.com.pokedex.feature.pokemoncompare.ui.intent.PokemonCompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.reducer.PokemonCompareReducer
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.PokemonCompareState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonCompareViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val firstId: Int,
    private val secondId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonCompareState())
    val state: StateFlow<PokemonCompareState> = _state

    private val _events = Channel<PokemonCompareEvent>()
    val events = _events.receiveAsFlow()

    init {
        // As duas colunas carregam de forma independente e em paralelo.
        loadSide(CompareSide.FIRST)
        loadSide(CompareSide.SECOND)
    }

    fun onIntent(intent: PokemonCompareIntent) {
        when (intent) {
            is PokemonCompareIntent.Retry -> loadSide(intent.side)
            is PokemonCompareIntent.NavigateBack -> {
                viewModelScope.launch { _events.send(PokemonCompareEvent.NavigateBack) }
            }
        }
    }

    private fun loadSide(side: CompareSide) {
        val id = if (side == CompareSide.FIRST) firstId else secondId
        viewModelScope.launch {
            _state.update { PokemonCompareReducer.loading(it, side) }
            when (val result = getPokemonDetailUseCase(id)) {
                is Result.Success ->
                    _state.update { PokemonCompareReducer.success(it, side, result.data.toCompareUiModel()) }
                is Result.Error ->
                    _state.update { PokemonCompareReducer.error(it, side, result.error) }
            }
        }
    }
}
