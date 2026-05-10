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
) : PokemonRepository {

    override fun getPokemonList(): Flow<PagingData<PokemonSummary>> =
        Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, prefetchDistance = PREFETCH_DISTANCE),
            pagingSourceFactory = { PokemonRemotePagingSource(remoteDataSource) },
        ).flow

    override suspend fun getPokemonDetail(id: Int): Result<Pokemon> {
        return when (val remote = remoteDataSource.getPokemonDetail(id)) {
            is Result.Success -> {
                val pokemon = remote.data.toDomain()
                localDataSource.savePokemonDetail(pokemon)
                Result.Success(pokemon)
            }
            is Result.Error -> {
                val cached = localDataSource.getPokemonDetail(id)
                if (cached != null) Result.Success(cached) else remote
            }
        }
    }
}
