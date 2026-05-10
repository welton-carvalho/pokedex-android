package br.com.pokedex.core.domain.usecase

import androidx.paging.PagingData
import br.com.pokedex.core.domain.repository.PokemonRepository
import br.com.pokedex.core.model.PokemonSummary
import kotlinx.coroutines.flow.Flow

class GetPokemonListUseCase(private val repository: PokemonRepository) {
    operator fun invoke(): Flow<PagingData<PokemonSummary>> = repository.getPokemonList()
}
