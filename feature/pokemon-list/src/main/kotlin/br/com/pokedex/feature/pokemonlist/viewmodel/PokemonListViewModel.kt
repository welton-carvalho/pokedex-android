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
        val previous = _state.value
        val next = PokemonListReducer.reduce(previous, intent)
        _state.value = next
        when (intent) {
            is PokemonListIntent.ClickPokemon -> {
                if (!previous.isCompareMode) {
                    viewModelScope.launch {
                        _events.send(PokemonListEvent.NavigateToDetail(intent.id))
                    }
                }
            }
            is PokemonListIntent.ToggleSelectForCompare -> handleCompareSelection(previous, next, intent.id)
            is PokemonListIntent.Retry,
            is PokemonListIntent.ToggleCompareMode,
            is PokemonListIntent.ResetCompareMode -> Unit
        }
    }

    private fun handleCompareSelection(
        previous: PokemonListState,
        next: PokemonListState,
        id: Int,
    ) {
        if (!previous.isCompareMode) return
        val stateChanged = previous.selectedForCompare != next.selectedForCompare
        when {
            stateChanged && next.selectedForCompare.size == 2 -> {
                val ids = next.selectedForCompare.toList()
                viewModelScope.launch {
                    _events.send(PokemonListEvent.NavigateToCompare(ids[0], ids[1]))
                }
            }
            !stateChanged && previous.selectedForCompare.size == 2 && id !in previous.selectedForCompare -> {
                viewModelScope.launch {
                    _events.send(PokemonListEvent.ShowSelectionLimitReached)
                }
            }
        }
    }
}
