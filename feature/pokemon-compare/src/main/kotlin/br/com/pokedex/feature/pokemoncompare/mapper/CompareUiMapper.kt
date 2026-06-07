package br.com.pokedex.feature.pokemoncompare.mapper

import br.com.pokedex.core.designsystem.theme.pokemonTypeColor
import br.com.pokedex.core.designsystem.util.PokemonImageUrlProvider
import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareStatUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatComparisonRow

private val statLabelMap = mapOf(
    "hp" to "HP",
    "attack" to "ATK",
    "defense" to "DEF",
    "special-attack" to "SATK",
    "special-defense" to "SDEF",
    "speed" to "SPD",
)

fun Pokemon.toCompareUiModel(): CompareUiModel {
    val primaryType = types.minByOrNull { it.slot }?.name ?: "normal"
    return CompareUiModel(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        number = "#${id.toString().padStart(3, '0')}",
        imageUrl = PokemonImageUrlProvider.officialArtwork(id),
        typeColor = pokemonTypeColor(primaryType),
        types = types.sortedBy { it.slot }.map { it.name },
        stats = stats.map { stat ->
            CompareStatUiModel(
                label = statLabelMap[stat.name] ?: stat.name.uppercase(),
                value = stat.baseStat,
            )
        },
    )
}

fun buildStatComparison(
    first: List<CompareStatUiModel>,
    second: List<CompareStatUiModel>,
): List<StatComparisonRow> =
    first.zip(second) { a, b ->
        StatComparisonRow(
            label = a.label,
            firstValue = a.value,
            secondValue = b.value,
            firstWins = a.value >= b.value,
            secondWins = b.value >= a.value,
        )
    }
