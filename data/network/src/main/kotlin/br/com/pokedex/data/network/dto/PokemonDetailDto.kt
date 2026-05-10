package br.com.pokedex.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TypeSlotDto>,
    val stats: List<StatDto>,
    val abilities: List<AbilitySlotDto>,
)

@Serializable
data class TypeSlotDto(
    val slot: Int,
    val type: TypeDto,
)

@Serializable
data class TypeDto(val name: String, val url: String)

@Serializable
data class StatDto(
    @SerialName("base_stat") val baseStat: Int,
    val stat: StatNameDto,
)

@Serializable
data class StatNameDto(val name: String, val url: String)

@Serializable
data class AbilitySlotDto(
    @SerialName("is_hidden") val isHidden: Boolean,
    val slot: Int,
    val ability: AbilityNameDto,
)

@Serializable
data class AbilityNameDto(val name: String, val url: String)
