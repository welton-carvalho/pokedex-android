package br.com.pokedex.data.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonSpeciesDto(
    @SerialName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntryDto>,
)

@Serializable
data class FlavorTextEntryDto(
    @SerialName("flavor_text") val flavorText: String,
    val language: LanguageDto,
)

@Serializable
data class LanguageDto(val name: String)
