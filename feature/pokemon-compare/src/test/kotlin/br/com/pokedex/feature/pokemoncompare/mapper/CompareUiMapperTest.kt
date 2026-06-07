package br.com.pokedex.feature.pokemoncompare.mapper

import br.com.pokedex.core.testing.fake.FakePokemonData
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareStatUiModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompareUiMapperTest {

    @Test
    fun `toCompareUiModel maps name capitalized`() {
        val model = FakePokemonData.bulbasaur.toCompareUiModel()
        assertEquals("Bulbasaur", model.name)
    }

    @Test
    fun `toCompareUiModel formats number with leading zeros`() {
        val model = FakePokemonData.bulbasaur.toCompareUiModel()
        assertEquals("#001", model.number)
    }

    @Test
    fun `toCompareUiModel maps all 6 stats`() {
        val model = FakePokemonData.bulbasaur.toCompareUiModel()
        assertEquals(6, model.stats.size)
    }

    @Test
    fun `toCompareUiModel uses statLabelMap abbreviations`() {
        val model = FakePokemonData.bulbasaur.toCompareUiModel()
        val labels = model.stats.map { it.label }
        assertTrue(labels.containsAll(listOf("HP", "ATK", "DEF", "SATK", "SDEF", "SPD")))
    }

    @Test
    fun `buildStatComparison first wins when greater`() {
        val first = listOf(CompareStatUiModel("HP", 50))
        val second = listOf(CompareStatUiModel("HP", 40))
        val result = buildStatComparison(first, second)
        assertTrue(result[0].firstWins)
        assertFalse(result[0].secondWins)
    }

    @Test
    fun `buildStatComparison second wins when greater`() {
        val first = listOf(CompareStatUiModel("HP", 30))
        val second = listOf(CompareStatUiModel("HP", 45))
        val result = buildStatComparison(first, second)
        assertFalse(result[0].firstWins)
        assertTrue(result[0].secondWins)
    }

    @Test
    fun `buildStatComparison both win on tie`() {
        val first = listOf(CompareStatUiModel("HP", 45))
        val second = listOf(CompareStatUiModel("HP", 45))
        val result = buildStatComparison(first, second)
        assertTrue(result[0].firstWins)
        assertTrue(result[0].secondWins)
    }
}
