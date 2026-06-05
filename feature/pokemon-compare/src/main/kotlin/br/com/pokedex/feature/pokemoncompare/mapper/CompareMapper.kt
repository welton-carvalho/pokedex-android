package br.com.pokedex.feature.pokemoncompare.mapper

import br.com.pokedex.core.designsystem.theme.pokemonTypeColor
import br.com.pokedex.core.designsystem.util.PokemonImageUrlProvider
import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatUiModel

private val statLabelMap = mapOf(
    "hp" to "HP",
    "attack" to "ATK",
    "defense" to "DEF",
    "special-attack" to "SPA",
    "special-defense" to "SPD",
    "speed" to "SPE",
)

private val statOrder = listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed")

fun Pokemon.toCompareUiModel(): CompareUiModel {
    val sortedTypes = types.sortedBy { it.slot }
    val primaryTypeName = sortedTypes.firstOrNull()?.name ?: "normal"
    val statsByName = stats.associateBy { it.name }
    val orderedStats = statOrder.mapNotNull { key ->
        statsByName[key]?.let {
            StatUiModel(
                label = statLabelMap[key] ?: key.uppercase(),
                value = it.baseStat,
            )
        }
    }
    return CompareUiModel(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        formattedNumber = "#${id.toString().padStart(3, '0')}",
        imageUrl = PokemonImageUrlProvider.officialArtwork(id),
        types = sortedTypes.map { it.name.replaceFirstChar { c -> c.uppercase() } },
        primaryTypeColor = pokemonTypeColor(primaryTypeName),
        heightMeters = height / 10f,
        weightKg = weight / 10f,
        stats = orderedStats,
    )
}
