package br.com.pokedex.feature.pokemonlist.ui.reducer

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemonlist.ui.intent.PokemonListIntent
import br.com.pokedex.feature.pokemonlist.ui.state.PokemonListState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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

    @Test
    fun `ToggleCompareMode enables compare mode when off`() {
        val state = PokemonListState(isCompareMode = false)
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleCompareMode)
        assertTrue(result.isCompareMode)
    }

    @Test
    fun `ToggleCompareMode disables compare mode and clears selection`() {
        val state = PokemonListState(isCompareMode = true, selectedIds = listOf(1, 2))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleCompareMode)
        assertFalse(result.isCompareMode)
        assertTrue(result.selectedIds.isEmpty())
    }

    @Test
    fun `ToggleSelection adds id when not present and size under 2`() {
        val state = PokemonListState(selectedIds = emptyList())
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelection(id = 5))
        assertTrue(result.selectedIds.contains(5))
    }

    @Test
    fun `ToggleSelection removes id when already selected`() {
        val state = PokemonListState(selectedIds = listOf(5))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelection(id = 5))
        assertFalse(result.selectedIds.contains(5))
    }

    @Test
    fun `ToggleSelection does not add third id when 2 already selected`() {
        val state = PokemonListState(selectedIds = listOf(1, 2))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelection(id = 3))
        assertEquals(2, result.selectedIds.size)
        assertFalse(result.selectedIds.contains(3))
    }

    @Test
    fun `ToggleSelection does not duplicate id`() {
        val state = PokemonListState(selectedIds = listOf(1))
        val result1 = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelection(id = 1))
        assertFalse(result1.selectedIds.contains(1))
    }
}
