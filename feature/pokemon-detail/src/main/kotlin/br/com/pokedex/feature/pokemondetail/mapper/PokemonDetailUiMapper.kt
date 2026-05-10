package br.com.pokedex.feature.pokemondetail.mapper

import br.com.pokedex.core.designsystem.theme.pokemonTypeColor
import br.com.pokedex.core.designsystem.util.PokemonImageUrlProvider
import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.feature.pokemondetail.ui.model.PokemonDetailUiModel
import br.com.pokedex.feature.pokemondetail.ui.model.StatUiModel

private val statLabelMap = mapOf(
    "hp" to "HP",
    "attack" to "ATK",
    "defense" to "DEF",
    "special-attack" to "SATK",
    "special-defense" to "SDEF",
    "speed" to "SPD",
)

fun Pokemon.toDetailUiModel(): PokemonDetailUiModel {
    val primaryType = types.minByOrNull { it.slot }?.name ?: "normal"
    return PokemonDetailUiModel(
        id = id,
        name = name.replaceFirstChar { it.uppercase() },
        imageUrl = PokemonImageUrlProvider.officialArtwork(id),
        typeColor = pokemonTypeColor(primaryType),
        types = types.sortedBy { it.slot }.map { it.name },
        height = "${height / 10.0}m",
        weight = "${weight / 10.0}kg",
        abilities = abilities.filter { !it.isHidden }.map { it.name.replace('-', ' ').replaceFirstChar { c -> c.uppercase() } },
        stats = stats.map { stat ->
            StatUiModel(
                label = statLabelMap[stat.name] ?: stat.name.uppercase(),
                value = stat.baseStat,
            )
        },
    )
}
