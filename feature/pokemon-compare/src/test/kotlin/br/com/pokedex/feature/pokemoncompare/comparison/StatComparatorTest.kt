package br.com.pokedex.feature.pokemoncompare.comparison

import androidx.compose.ui.graphics.Color
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareStatUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatWinner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StatComparatorTest {

    private fun model(vararg stats: Pair<String, Int>) = ComparePokemonUiModel(
        id = 0,
        name = "X",
        imageUrl = "",
        typeColor = Color.Unspecified,
        types = emptyList(),
        height = "",
        weight = "",
        abilities = emptyList(),
        description = "",
        stats = stats.map { CompareStatUiModel(it.first, it.second) },
    )

    @Test
    fun `first wins when its value is higher`() {
        val rows = buildStatComparison(model("HP" to 50), model("HP" to 30))
        assertEquals(StatWinner.FIRST, rows.single().winner)
    }

    @Test
    fun `second wins when its value is higher`() {
        val rows = buildStatComparison(model("HP" to 30), model("HP" to 50))
        assertEquals(StatWinner.SECOND, rows.single().winner)
    }

    @Test
    fun `equal values produce a tie`() {
        val rows = buildStatComparison(model("HP" to 40), model("HP" to 40))
        assertEquals(StatWinner.TIE, rows.single().winner)
    }

    @Test
    fun `stats are aligned by label regardless of order`() {
        val rows = buildStatComparison(
            model("HP" to 10, "ATK" to 80),
            model("ATK" to 50, "HP" to 99),
        )
        val byLabel = rows.associateBy { it.label }
        assertEquals(StatWinner.SECOND, byLabel.getValue("HP").winner)
        assertEquals(StatWinner.FIRST, byLabel.getValue("ATK").winner)
    }

    @Test
    fun `missing stat on one side counts as zero`() {
        val rows = buildStatComparison(model("HP" to 10), model("ATK" to 10))
        val byLabel = rows.associateBy { it.label }
        assertEquals(StatWinner.FIRST, byLabel.getValue("HP").winner)   // 10 vs 0
        assertEquals(StatWinner.SECOND, byLabel.getValue("ATK").winner) // 0 vs 10
        assertEquals(2, rows.size)
    }
}
