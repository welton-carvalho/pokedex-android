package br.com.pokedex.core.designsystem.util

object PokemonImageUrlProvider {
    fun officialArtwork(id: Int): String =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
}
