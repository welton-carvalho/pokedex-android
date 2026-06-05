package br.com.pokedex.core.route.keys

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object PokemonListKey : NavKey

@Serializable
data class PokemonDetailKey(val pokemonId: Int) : NavKey

@Serializable
data class PokemonCompareKey(val idA: Int, val idB: Int) : NavKey
