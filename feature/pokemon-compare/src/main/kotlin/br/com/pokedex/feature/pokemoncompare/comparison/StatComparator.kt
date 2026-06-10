package br.com.pokedex.feature.pokemoncompare.comparison

import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatComparisonRow
import br.com.pokedex.feature.pokemoncompare.ui.model.StatWinner

/**
 * Alinha os stats dos dois Pokémon por label (preservando a ordem do primeiro)
 * e calcula o vencedor de cada stat. Stats ausentes em um dos lados contam como 0.
 */
fun buildStatComparison(
    first: ComparePokemonUiModel,
    second: ComparePokemonUiModel,
): List<StatComparisonRow> {
    val secondByLabel = second.stats.associate { it.label to it.value }
    val orderedLabels = LinkedHashSet<String>().apply {
        first.stats.forEach { add(it.label) }
        second.stats.forEach { add(it.label) }
    }
    val firstByLabel = first.stats.associate { it.label to it.value }

    return orderedLabels.map { label ->
        val firstValue = firstByLabel[label] ?: 0
        val secondValue = secondByLabel[label] ?: 0
        StatComparisonRow(
            label = label,
            firstValue = firstValue,
            secondValue = secondValue,
            winner = when {
                firstValue > secondValue -> StatWinner.FIRST
                secondValue > firstValue -> StatWinner.SECOND
                else -> StatWinner.TIE
            },
        )
    }
}
