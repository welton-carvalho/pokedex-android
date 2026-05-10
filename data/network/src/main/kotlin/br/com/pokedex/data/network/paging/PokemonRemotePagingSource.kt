package br.com.pokedex.data.network.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import br.com.pokedex.core.model.PokemonSummary
import br.com.pokedex.data.network.source.RemotePokemonDataSource

private const val PAGE_SIZE = 50
private const val STARTING_OFFSET = 0

class PokemonRemotePagingSource(
    private val dataSource: RemotePokemonDataSource,
) : PagingSource<Int, PokemonSummary>() {

    override fun getRefreshKey(state: PagingState<Int, PokemonSummary>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(PAGE_SIZE)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(PAGE_SIZE)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PokemonSummary> {
        val offset = params.key ?: STARTING_OFFSET
        return try {
            val response = dataSource.getPokemonList(offset, PAGE_SIZE)
            val items = response.results.map { dto ->
                PokemonSummary(id = dto.extractId(), name = dto.name)
            }
            LoadResult.Page(
                data = items,
                prevKey = if (offset == STARTING_OFFSET) null else offset - PAGE_SIZE,
                nextKey = if (response.next == null) null else offset + PAGE_SIZE,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
