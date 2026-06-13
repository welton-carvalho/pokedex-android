package br.com.pokedex.feature.pokemonlist.ui.reducer

import br.com.pokedex.feature.pokemonlist.ui.intent.PokemonListIntent
import br.com.pokedex.feature.pokemonlist.ui.state.PokemonListState

object PokemonListReducer {
    fun reduce(state: PokemonListState, intent: PokemonListIntent): PokemonListState =
        when (intent) {
            is PokemonListIntent.Retry -> state.copy(isLoading = true, error = null)
            is PokemonListIntent.ClickPokemon -> state
        }
}
