package br.com.pokedex.feature.pokemoncompare.ui.reducer

import androidx.compose.ui.graphics.Color
import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.feature.pokemoncompare.ui.intent.PokemonCompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.PokemonCompareState
import br.com.pokedex.feature.pokemoncompare.ui.state.SideUiState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PokemonCompareReducerTest {

    private val sample = ComparePokemonUiModel(
        id = 1, name = "Bulbasaur", imageUrl = "", typeColor = Color.Unspecified,
        types = emptyList(), height = "", weight = "", abilities = emptyList(),
        description = "", stats = emptyList(),
    )

    @Test
    fun `success updates only the targeted side`() {
        val state = PokemonCompareReducer.success(PokemonCompareState(), CompareSide.FIRST, sample)
        assertTrue(state.first is SideUiState.Success)
        assertEquals(SideUiState.Loading, state.second)
    }

    @Test
    fun `error updates only the targeted side`() {
        val state = PokemonCompareReducer.error(PokemonCompareState(), CompareSide.SECOND, DomainError.Network)
        assertEquals(SideUiState.Error(DomainError.Network), state.second)
        assertEquals(SideUiState.Loading, state.first)
    }

    @Test
    fun `retry sets the targeted side back to loading`() {
        val errored = PokemonCompareState(
            first = SideUiState.Success(sample),
            second = SideUiState.Error(DomainError.Timeout),
        )
        val state = PokemonCompareReducer.reduce(errored, PokemonCompareIntent.Retry(CompareSide.SECOND))
        assertEquals(SideUiState.Loading, state.second)
        assertTrue(state.first is SideUiState.Success) // outro lado preservado
    }

    @Test
    fun `navigate back does not change state`() {
        val state = PokemonCompareState(first = SideUiState.Success(sample))
        assertEquals(state, PokemonCompareReducer.reduce(state, PokemonCompareIntent.NavigateBack))
    }
}
