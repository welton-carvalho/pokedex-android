package br.com.pokedex.feature.pokemoncompare.mapper

import br.com.pokedex.core.designsystem.theme.TypeFire
import br.com.pokedex.core.designsystem.theme.TypeGrass
import br.com.pokedex.core.testing.fake.FakePokemonData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CompareMapperTest {

    @Test
    fun `maps id name formattedNumber and imageUrl correctly`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel()
        assertEquals(1, ui.id)
        assertEquals("Bulbasaur", ui.name)
        assertEquals("#001", ui.formattedNumber)
        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            ui.imageUrl,
        )
    }

    @Test
    fun `types are ordered by slot and capitalized`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel()
        assertEquals(listOf("Grass", "Poison"), ui.types)
    }

    @Test
    fun `primaryTypeColor uses the lowest-slot type`() {
        val grass = FakePokemonData.bulbasaur.toCompareUiModel()
        val fire = FakePokemonData.charmander.toCompareUiModel()
        assertEquals(TypeGrass, grass.primaryTypeColor)
        assertEquals(TypeFire, fire.primaryTypeColor)
    }

    @Test
    fun `height and weight are normalized to meters and kilograms`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel()
        assertEquals(0.7f, ui.heightMeters)
        assertEquals(6.9f, ui.weightKg)
    }

    @Test
    fun `stats are ordered HP ATK DEF SPA SPD SPE`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel()
        assertEquals(
            listOf("HP", "ATK", "DEF", "SPA", "SPD", "SPE"),
            ui.stats.map { it.label },
        )
        assertEquals(listOf(45, 49, 49, 65, 65, 45), ui.stats.map { it.value })
    }

    @Test
    fun `stat values default maxValue is 255`() {
        val ui = FakePokemonData.bulbasaur.toCompareUiModel()
        ui.stats.forEach { stat ->
            assertEquals(255, stat.maxValue)
        }
    }
}
