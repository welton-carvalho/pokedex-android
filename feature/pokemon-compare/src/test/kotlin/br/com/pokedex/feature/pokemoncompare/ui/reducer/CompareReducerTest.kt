package br.com.pokedex.feature.pokemoncompare.ui.reducer

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.intent.CompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompareReducerTest {

    @Test
    fun `loading sets isLoading true and clears error`() {
        val state = CompareState(isLoading = false, error = DomainError.Network)
        val result = CompareReducer.loading(state)
        assertTrue(result.isLoading)
        assertNull(result.error)
    }

    @Test
    fun `error sets isLoading false and stores error`() {
        val state = CompareState(isLoading = true)
        val result = CompareReducer.error(state, DomainError.Timeout)
        assertTrue(!result.isLoading)
        assertEquals(DomainError.Timeout, result.error)
    }

    @Test
    fun `Retry intent transitions to loading`() {
        val state = CompareState(isLoading = false, error = DomainError.Network)
        val result = CompareReducer.reduce(state, CompareIntent.Retry)
        assertTrue(result.isLoading)
        assertNull(result.error)
    }

    @Test
    fun `NavigateBack intent does not change state`() {
        val state = CompareState(isLoading = false, error = null)
        val result = CompareReducer.reduce(state, CompareIntent.NavigateBack)
        assertEquals(state, result)
    }
}
