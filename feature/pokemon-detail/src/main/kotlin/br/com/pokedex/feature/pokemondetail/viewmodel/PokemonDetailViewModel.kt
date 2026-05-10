package br.com.pokedex.feature.pokemondetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.feature.pokemondetail.mapper.toDetailUiModel
import br.com.pokedex.feature.pokemondetail.ui.event.PokemonDetailEvent
import br.com.pokedex.feature.pokemondetail.ui.intent.PokemonDetailIntent
import br.com.pokedex.feature.pokemondetail.ui.reducer.PokemonDetailReducer
import br.com.pokedex.feature.pokemondetail.ui.state.PokemonDetailState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase,
    private val pokemonId: Int,
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonDetailState())
    val state: StateFlow<PokemonDetailState> = _state

    private val _events = Channel<PokemonDetailEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadPokemon()
    }

    fun onIntent(intent: PokemonDetailIntent) {
        _state.update { PokemonDetailReducer.reduce(it, intent) }
        when (intent) {
            is PokemonDetailIntent.Retry -> loadPokemon()
            is PokemonDetailIntent.NavigateBack -> {
                viewModelScope.launch { _events.send(PokemonDetailEvent.NavigateBack) }
            }
        }
    }

    private fun loadPokemon() {
        viewModelScope.launch {
            _state.update { PokemonDetailReducer.loading(it) }
            when (val result = getPokemonDetailUseCase(pokemonId)) {
                is Result.Success -> _state.update { PokemonDetailReducer.success(it, result.data.toDetailUiModel()) }
                is Result.Error -> _state.update { PokemonDetailReducer.error(it, result.error) }
            }
        }
    }
}
