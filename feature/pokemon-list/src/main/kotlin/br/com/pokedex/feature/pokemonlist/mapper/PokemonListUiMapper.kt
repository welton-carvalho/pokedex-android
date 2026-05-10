package br.com.pokedex.feature.pokemonlist.mapper

import br.com.pokedex.core.designsystem.util.PokemonImageUrlProvider
import br.com.pokedex.core.model.PokemonSummary
import br.com.pokedex.feature.pokemonlist.ui.model.PokemonListUiModel

fun PokemonSummary.toUiModel(): PokemonListUiModel = PokemonListUiModel(
    id = id,
    displayName = name.replaceFirstChar { it.uppercase() },
    imageUrl = PokemonImageUrlProvider.officialArtwork(id),
)
