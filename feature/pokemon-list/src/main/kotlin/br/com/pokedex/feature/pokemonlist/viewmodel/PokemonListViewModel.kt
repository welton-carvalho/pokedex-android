package br.com.pokedex.feature.pokemonlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import br.com.pokedex.core.domain.usecase.GetPokemonListUseCase
import br.com.pokedex.feature.pokemonlist.mapper.toUiModel
import br.com.pokedex.feature.pokemonlist.ui.event.PokemonListEvent
import br.com.pokedex.feature.pokemonlist.ui.intent.PokemonListIntent
import br.com.pokedex.feature.pokemonlist.ui.model.PokemonListUiModel
import br.com.pokedex.feature.pokemonlist.ui.reducer.PokemonListReducer
import br.com.pokedex.feature.pokemonlist.ui.state.PokemonListState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PokemonListViewModel(
    getPokemonListUseCase: GetPokemonListUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PokemonListState())
    val state: StateFlow<PokemonListState> = _state

    private val _events = Channel<PokemonListEvent>()
    val events = _events.receiveAsFlow()

    val pokemonPagingFlow: Flow<PagingData<PokemonListUiModel>> =
        getPokemonListUseCase()
            .map { pagingData -> pagingData.map { it.toUiModel() } }
            .cachedIn(viewModelScope)

    fun onIntent(intent: PokemonListIntent) {
        // Detecta tentativa de selecionar um 3º Pokémon ANTES de aplicar o reducer (estado fica inalterado).
        val selectionLimitReached = intent is PokemonListIntent.ToggleSelection &&
            intent.id !in _state.value.selectedIds &&
            _state.value.selectedIds.size >= PokemonListState.MAX_COMPARE_SELECTION

        // Captura os ids ANTES do reducer/limpeza para a navegação de comparação.
        val compareIds = if (intent is PokemonListIntent.ClickCompare) _state.value.selectedIds else emptyList()

        _state.update { PokemonListReducer.reduce(it, intent) }

        when (intent) {
            is PokemonListIntent.ClickPokemon -> {
                viewModelScope.launch {
                    _events.send(PokemonListEvent.NavigateToDetail(intent.id))
                }
            }
            is PokemonListIntent.ToggleSelection -> {
                if (selectionLimitReached) {
                    viewModelScope.launch { _events.send(PokemonListEvent.ShowSelectionLimit) }
                }
            }
            is PokemonListIntent.ClickCompare -> {
                if (compareIds.size == PokemonListState.MAX_COMPARE_SELECTION) {
                    viewModelScope.launch {
                        _events.send(PokemonListEvent.NavigateToCompare(compareIds[0], compareIds[1]))
                    }
                    // Q4=A: limpa seleção e sai do modo ao navegar
                    _state.update { it.copy(isSelectionMode = false, selectedIds = emptyList()) }
                }
            }
            is PokemonListIntent.Retry,
            is PokemonListIntent.ToggleSelectionMode -> Unit
        }
    }
}
