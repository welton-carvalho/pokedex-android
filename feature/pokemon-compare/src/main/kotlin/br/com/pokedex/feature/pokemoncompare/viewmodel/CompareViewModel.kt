package br.com.pokedex.feature.pokemoncompare.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.pokedex.core.common.dispatcher.DispatcherProvider
import br.com.pokedex.core.common.error.ErrorHandler
import br.com.pokedex.core.common.result.AsyncResult
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.feature.pokemoncompare.mapper.toCompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.intent.CompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.intent.Side
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompareViewModel(
    private val idA: Int,
    private val idB: Int,
    private val getPokemonDetail: GetPokemonDetailUseCase,
    private val dispatcherProvider: DispatcherProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(CompareState())
    val state: StateFlow<CompareState> = _state.asStateFlow()

    private var jobA: Job? = null
    private var jobB: Job? = null

    init {
        if (idA == idB) {
            _state.update { it.copy(isInvalidInput = true) }
        } else {
            loadSide(Side.A)
            loadSide(Side.B)
        }
    }

    fun onIntent(intent: CompareIntent) {
        when (intent) {
            is CompareIntent.RetryPokemon -> retry(intent.side)
        }
    }

    private fun retry(side: Side) {
        when (side) {
            Side.A -> jobA?.cancel()
            Side.B -> jobB?.cancel()
        }
        loadSide(side)
    }

    private fun loadSide(side: Side) {
        val id = if (side == Side.A) idA else idB
        setLoading(side)
        val job = viewModelScope.launch {
            try {
                val result = withContext(dispatcherProvider.io) { getPokemonDetail(id) }
                when (result) {
                    is Result.Success -> setSuccess(side, result.data.toCompareUiModel())
                    is Result.Error -> setError(side, AsyncResult.Error(result.error))
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                setError(side, AsyncResult.Error(ErrorHandler.handle(throwable)))
            }
        }
        when (side) {
            Side.A -> jobA = job
            Side.B -> jobB = job
        }
    }

    private fun setLoading(side: Side) {
        _state.update { current ->
            when (side) {
                Side.A -> current.copy(pokemonA = AsyncResult.Loading)
                Side.B -> current.copy(pokemonB = AsyncResult.Loading)
            }
        }
    }

    private fun setSuccess(side: Side, value: br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel) {
        _state.update { current ->
            when (side) {
                Side.A -> current.copy(pokemonA = AsyncResult.Success(value))
                Side.B -> current.copy(pokemonB = AsyncResult.Success(value))
            }
        }
    }

    private fun setError(side: Side, error: AsyncResult.Error) {
        _state.update { current ->
            when (side) {
                Side.A -> current.copy(pokemonA = error)
                Side.B -> current.copy(pokemonB = error)
            }
        }
    }
}
