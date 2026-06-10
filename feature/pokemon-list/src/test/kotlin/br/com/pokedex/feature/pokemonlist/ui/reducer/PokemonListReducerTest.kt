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
    fun `ToggleSelectionMode enables selection mode`() {
        val result = PokemonListReducer.reduce(PokemonListState(), PokemonListIntent.ToggleSelectionMode)
        assertTrue(result.isSelectionMode)
    }

    @Test
    fun `ToggleSelectionMode off clears selection`() {
        val state = PokemonListState(isSelectionMode = true, selectedIds = listOf(1, 4))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelectionMode)
        assertFalse(result.isSelectionMode)
        assertTrue(result.selectedIds.isEmpty())
    }

    @Test
    fun `ToggleSelection adds an unselected id`() {
        val result = PokemonListReducer.reduce(PokemonListState(), PokemonListIntent.ToggleSelection(1))
        assertEquals(listOf(1), result.selectedIds)
    }

    @Test
    fun `ToggleSelection removes an already selected id`() {
        val state = PokemonListState(selectedIds = listOf(1, 4))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelection(1))
        assertEquals(listOf(4), result.selectedIds)
    }

    @Test
    fun `ToggleSelection is a no-op when two are already selected`() {
        val state = PokemonListState(selectedIds = listOf(1, 4))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelection(7))
        assertEquals(listOf(1, 4), result.selectedIds)
    }

    @Test
    fun `canCompare is true only with exactly two selected`() {
        assertFalse(PokemonListState(selectedIds = listOf(1)).canCompare)
        assertTrue(PokemonListState(selectedIds = listOf(1, 4)).canCompare)
    }
}
