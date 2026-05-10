package br.com.pokedex.core.model

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<PokemonType>,
    val stats: List<PokemonStat>,
    val abilities: List<PokemonAbility>,
    val height: Int,
    val weight: Int,
    val description: String = "",
)

data class PokemonType(
    val slot: Int,
    val name: String,
)

data class PokemonStat(
    val name: String,
    val baseStat: Int,
)

data class PokemonAbility(
    val name: String,
    val isHidden: Boolean,
)

data class PokemonSummary(
    val id: Int,
    val name: String,
)
