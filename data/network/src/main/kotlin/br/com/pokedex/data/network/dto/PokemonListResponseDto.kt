package br.com.pokedex.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponseDto(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItemDto>,
)

@Serializable
data class PokemonListItemDto(
    val name: String,
    val url: String,
) {
    fun extractId(): Int = url.trimEnd('/').substringAfterLast('/').toInt()
}
