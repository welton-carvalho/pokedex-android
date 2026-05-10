package br.com.pokedex.core.domain.usecase

import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.repository.PokemonRepository
import br.com.pokedex.core.model.Pokemon

class GetPokemonDetailUseCase(private val repository: PokemonRepository) {
    suspend operator fun invoke(id: Int): Result<Pokemon> = repository.getPokemonDetail(id)
}
