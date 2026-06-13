package br.com.pokedex.feature.pokemonlist.ui.reducer

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemonlist.ui.intent.PokemonListIntent
import br.com.pokedex.feature.pokemonlist.ui.state.PokemonListState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PokemonListReducerTest {

    @Test
    fun `Retry intent sets isLoading true`() {
        val state = PokemonListState(isLoading = false)
        val result = PokemonListReducer.reduce(state, PokemonListIntent.Retry)
        assertTrue(result.isLoading)
    }

    @Test
    fun `Retry intent clears existing error`() {
        val state = PokemonListState(error = DomainError.Network)
        val result = PokemonListReducer.reduce(state, PokemonListIntent.Retry)
        assertNull(result.error)
    }

    @Test
    fun `ClickPokemon intent does not change state`() {
        val state = PokemonListState(isLoading = false, error = null)
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ClickPokemon(id = 1))
        assertEquals(state, result)
    }

    @Test
    fun `Retry from error state resets both loading and error correctly`() {
        val state = PokemonListState(isLoading = false, error = DomainError.Timeout)
        val result = PokemonListReducer.reduce(state, PokemonListIntent.Retry)
        assertTrue(result.isLoading)
        assertNull(result.error)
    }
}
