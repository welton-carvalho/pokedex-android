package br.com.pokedex.core.domain.repository

import androidx.paging.PagingData
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.model.Pokemon
import br.com.pokedex.core.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    fun getPokemonList(): Flow<PagingData<PokemonSummary>>
    suspend fun getPokemonDetail(id: Int): Result<Pokemon>
}
