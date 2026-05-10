package br.com.pokedex.data.local.source

import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.core.model.PokemonSummary

class LocalPokemonDataSource {
    private val detailCache = mutableMapOf<Int, Pokemon>()
    private val summaryCache = mutableListOf<PokemonSummary>()

    fun getPokemonDetail(id: Int): Pokemon? = detailCache[id]

    fun savePokemonDetail(pokemon: Pokemon) {
        detailCache[pokemon.id] = pokemon
    }

    fun getPokemonSummaries(): List<PokemonSummary> = summaryCache.toList()

    fun savePokemonSummaries(summaries: List<PokemonSummary>) {
        summaryCache.clear()
        summaryCache.addAll(summaries)
    }
}
