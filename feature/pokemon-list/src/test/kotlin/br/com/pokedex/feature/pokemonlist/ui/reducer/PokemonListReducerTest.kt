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
    fun `ToggleCompareMode activates compare mode and preserves selection`() {
        val state = PokemonListState(isCompareMode = false, selectedForCompare = emptySet())
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleCompareMode)
        assertTrue(result.isCompareMode)
        assertTrue(result.selectedForCompare.isEmpty())
    }

    @Test
    fun `ToggleCompareMode deactivates compare mode and clears selection`() {
        val state = PokemonListState(isCompareMode = true, selectedForCompare = setOf(1))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleCompareMode)
        assertFalse(result.isCompareMode)
        assertTrue(result.selectedForCompare.isEmpty())
    }

    @Test
    fun `ToggleSelectForCompare when not in compare mode is ignored`() {
        val state = PokemonListState(isCompareMode = false, selectedForCompare = emptySet())
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelectForCompare(id = 1))
        assertEquals(state, result)
    }

    @Test
    fun `ToggleSelectForCompare adds id when size less than 2`() {
        val state = PokemonListState(isCompareMode = true, selectedForCompare = emptySet())
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelectForCompare(id = 1))
        assertEquals(setOf(1), result.selectedForCompare)
    }

    @Test
    fun `ToggleSelectForCompare removes id when already selected (deselect)`() {
        val state = PokemonListState(isCompareMode = true, selectedForCompare = setOf(1))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelectForCompare(id = 1))
        assertTrue(result.selectedForCompare.isEmpty())
    }

    @Test
    fun `ToggleSelectForCompare ignores new id when size already 2 (limit-ignored)`() {
        val state = PokemonListState(isCompareMode = true, selectedForCompare = setOf(1, 4))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelectForCompare(id = 7))
        assertEquals(setOf(1, 4), result.selectedForCompare)
        assertEquals(state, result)
    }

    @Test
    fun `ToggleSelectForCompare guarantees uniqueness via Set semantics`() {
        val state = PokemonListState(isCompareMode = true, selectedForCompare = setOf(1))
        val firstToggle = PokemonListReducer.reduce(state, PokemonListIntent.ToggleSelectForCompare(id = 1))
        val secondToggle = PokemonListReducer.reduce(firstToggle, PokemonListIntent.ToggleSelectForCompare(id = 1))
        assertTrue(firstToggle.selectedForCompare.isEmpty())
        assertEquals(setOf(1), secondToggle.selectedForCompare)
    }

    @Test
    fun `ResetCompareMode returns to defaults`() {
        val state = PokemonListState(isCompareMode = true, selectedForCompare = setOf(1, 4))
        val result = PokemonListReducer.reduce(state, PokemonListIntent.ResetCompareMode)
        assertFalse(result.isCompareMode)
        assertTrue(result.selectedForCompare.isEmpty())
    }
}
