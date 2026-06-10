package br.com.pokedex.feature.pokemoncompare.mapper

import br.com.pokedex.core.testing.fake.FakePokemonData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PokemonCompareUiMapperTest {

    @Test
    fun `maps name with capitalized first char`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel()
        assertEquals("Bulbasaur", ui.name)
    }

    @Test
    fun `formats height and weight in meters and kilograms`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel() // height=7, weight=69
        assertEquals("0.7m", ui.height)
        assertEquals("6.9kg", ui.weight)
    }

    @Test
    fun `hidden abilities are filtered out`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel() // overgrow (visible), chlorophyll (hidden)
        assertTrue(ui.abilities.any { it.equals("Overgrow", ignoreCase = true) })
        assertFalse(ui.abilities.any { it.equals("Chlorophyll", ignoreCase = true) })
    }

    @Test
    fun `stat labels are mapped to short codes`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel()
        val labels = ui.stats.map { it.label }
        assertTrue(labels.containsAll(listOf("HP", "ATK", "DEF", "SATK", "SDEF", "SPD")))
    }

    @Test
    fun `stat values are preserved`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel()
        val hp = ui.stats.first { it.label == "HP" }
        assertEquals(45, hp.value)
    }
}
