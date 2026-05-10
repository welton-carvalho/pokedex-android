package br.com.pokedex.core.navigation

import kotlinx.serialization.Serializable

@Serializable
data object PokemonListRoute

@Serializable
data class PokemonDetailRoute(val pokemonId: Int)
