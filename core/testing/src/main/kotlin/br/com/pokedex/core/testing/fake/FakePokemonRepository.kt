package br.com.pokedex.core.testing.fake

import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.repository.PokemonRepository
import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.core.model.PokemonSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePokemonRepository : PokemonRepository {

    var pokemonDetail: Pokemon = FakePokemonData.bulbasaur
    var detailError: DomainError? = null
    var summaries: List<PokemonSummary> = FakePokemonData.summaries

    override fun getPokemonList(): Flow<PagingData<PokemonSummary>> =
        flowOf(PagingData.from(summaries))

    override suspend fun getPokemonDetail(id: Int): Result<Pokemon> =
        detailError?.let { Result.Error(it) } ?: Result.Success(pokemonDetail)
}
