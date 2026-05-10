package br.com.pokedex.data.network.source

import br.com.pokedex.core.common.error.ErrorHandler
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.data.network.api.PokemonApiService
import br.com.pokedex.data.network.dto.PokemonDetailDto
import br.com.pokedex.data.network.dto.PokemonListResponseDto

class RemotePokemonDataSource(private val api: PokemonApiService) {

    suspend fun getPokemonList(offset: Int, limit: Int): PokemonListResponseDto =
        api.getPokemonList(offset, limit)

    suspend fun getPokemonDetail(id: Int): Result<PokemonDetailDto> = try {
        Result.Success(api.getPokemonDetail(id))
    } catch (e: Exception) {
        Result.Error(ErrorHandler.handle(e))
    }
}
