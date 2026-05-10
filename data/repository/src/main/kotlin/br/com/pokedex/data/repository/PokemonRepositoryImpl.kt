package br.com.pokedex.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.repository.PokemonRepository
import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.core.model.PokemonSummary
import br.com.pokedex.data.local.source.LocalPokemonDataSource
import br.com.pokedex.data.network.paging.PokemonRemotePagingSource
import br.com.pokedex.data.network.source.RemotePokemonDataSource
import br.com.pokedex.data.repository.mapper.toDomain
import kotlinx.coroutines.flow.Flow

private const val PAGE_SIZE = 50
private const val PREFETCH_DISTANCE = 10

class PokemonRepositoryImpl(
    private val remoteDataSource: RemotePokemonDataSource,
    private val localDataSource: LocalPokemonDataSource,
    private val cacheStrategy: CacheStrategy = CacheStrategy.NETWORK_FIRST,
) : PokemonRepository {

    override fun getPokemonList(): Flow<PagingData<PokemonSummary>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PREFETCH_DISTANCE),
            pagingSourceFactory = { PokemonRemotePagingSource(remoteDataSource) },
        ).flow

    override suspend fun getPokemonDetail(id: Int): Result<Pokemon> = when (cacheStrategy) {
        CacheStrategy.NETWORK_ONLY -> fetchFromRemote(id)
        CacheStrategy.CACHE_FIRST, CacheStrategy.DISK, CacheStrategy.MEMORY -> fetchCacheFirst(id)
        CacheStrategy.NETWORK_FIRST -> fetchNetworkFirst(id)
    }

    private suspend fun fetchNetworkFirst(id: Int): Result<Pokemon> {
        return when (val remote = remoteDataSource.getPokemonDetail(id)) {
            is Result.Success -> {
                val pokemon = remote.data.toDomain(fetchDescription(id))
                localDataSource.savePokemonDetail(pokemon)
                Result.Success(pokemon)
            }
            is Result.Error -> {
                val cached = localDataSource.getPokemonDetail(id)
                if (cached != null) Result.Success(cached) else remote
            }
        }
    }

    private suspend fun fetchCacheFirst(id: Int): Result<Pokemon> {
        val cached = localDataSource.getPokemonDetail(id)
        if (cached != null) return Result.Success(cached)
        return fetchFromRemote(id)
    }

    private suspend fun fetchFromRemote(id: Int): Result<Pokemon> {
        return when (val remote = remoteDataSource.getPokemonDetail(id)) {
            is Result.Success -> {
                val pokemon = remote.data.toDomain(fetchDescription(id))
                localDataSource.savePokemonDetail(pokemon)
                Result.Success(pokemon)
            }
            is Result.Error -> remote
        }
    }

    private suspend fun fetchDescription(id: Int): String {
        return when (val species = remoteDataSource.getPokemonSpecies(id)) {
            is Result.Success -> species.data.flavorTextEntries
                .firstOrNull { it.language.name == "en" }
                ?.flavorText
                ?.replace(Regex("[\\n\\f\\r]"), " ")
                ?.replace(Regex("\\s+"), " ")
                ?.trim()
                ?: ""
            is Result.Error -> ""
        }
    }
}
