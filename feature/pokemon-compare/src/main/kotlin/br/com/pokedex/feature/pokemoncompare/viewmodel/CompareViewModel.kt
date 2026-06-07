package br.com.pokedex.feature.pokemoncompare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.feature.pokemoncompare.mapper.buildStatComparison
import br.com.pokedex.feature.pokemoncompare.mapper.toCompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.event.CompareEvent
import br.com.pokedex.feature.pokemoncompare.ui.intent.CompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.reducer.CompareReducer
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareState
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompareViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val firstId: Int,
    private val secondId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(CompareState())
    val state: StateFlow<CompareState> = _state

    private val _events = Channel<CompareEvent>()
    val events = _events.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: CompareIntent) {
        _state.update { CompareReducer.reduce(it, intent) }
        when (intent) {
            is CompareIntent.Retry -> load()
            is CompareIntent.NavigateBack -> {
                viewModelScope.launch { _events.send(CompareEvent.NavigateBack) }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { CompareReducer.loading(it) }
            coroutineScope {
                val firstDeferred = async { getPokemonDetailUseCase(firstId) }
                val secondDeferred = async { getPokemonDetailUseCase(secondId) }
                val firstResult = firstDeferred.await()
                val secondResult = secondDeferred.await()

                when {
                    firstResult is Result.Success && secondResult is Result.Success -> {
                        val firstModel = firstResult.data.toCompareUiModel()
                        val secondModel = secondResult.data.toCompareUiModel()
                        _state.update { CompareReducer.success(it, firstModel, secondModel) }
                    }
                    firstResult is Result.Error -> {
                        _state.update { CompareReducer.error(it, firstResult.error) }
                    }
                    secondResult is Result.Error -> {
                        _state.update { CompareReducer.error(it, (secondResult as Result.Error).error) }
                    }
                }
            }
        }
    }
}
