package br.com.pokedex.data.local.source

import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.core.model.PokemonSummary
import br.com.pokedex.data.local.entity.PokemonDetailEntity
import br.com.pokedex.data.local.entity.PokemonSummaryEntity
import br.com.pokedex.data.local.mapper.toDomain
import br.com.pokedex.data.local.mapper.toEntity
import io.objectbox.Box

private const val CACHE_TTL_MS = 60 * 60 * 1000L // 1 hour

class LocalPokemonDataSource(
    private val summaryBox: Box<PokemonSummaryEntity>,
    private val detailBox: Box<PokemonDetailEntity>,
) {
    fun getPokemonDetail(id: Int): Pokemon? {
        val entity = detailBox.all.firstOrNull { it.pokemonId == id } ?: return null
        val isExpired = System.currentTimeMillis() - entity.cachedAt > CACHE_TTL_MS
        return if (isExpired) null else entity.toDomain()
    }

    fun savePokemonDetail(pokemon: Pokemon) {
        val existing = detailBox.all.firstOrNull { it.pokemonId == pokemon.id }
        val entity = pokemon.toEntity().copy(dbId = existing?.dbId ?: 0)
        detailBox.put(entity)
    }

    fun getPokemonSummaries(): List<PokemonSummary> =
        summaryBox.all.map { it.toDomain() }

    fun savePokemonSummaries(summaries: List<PokemonSummary>) {
        summaryBox.removeAll()
        summaryBox.put(summaries.map { it.toEntity() })
    }

    fun clearAll() {
        summaryBox.removeAll()
        detailBox.removeAll()
    }
}
