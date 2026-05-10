package br.com.pokedex.core.domain.usecase

import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.testing.fake.FakePokemonData
import br.com.pokedex.core.testing.fake.FakePokemonRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPokemonDetailUseCaseTest {

    private lateinit var repository: FakePokemonRepository
    private lateinit var useCase: GetPokemonDetailUseCase

    @BeforeEach
    fun setUp() {
        repository = FakePokemonRepository()
        useCase = GetPokemonDetailUseCase(repository)
    }

    @Test
    fun `returns success with pokemon from repository`() = runTest {
        repository.pokemonDetail = FakePokemonData.bulbasaur

        val result = useCase(1)

        assertEquals(Result.Success(FakePokemonData.bulbasaur), result)
    }

    @Test
    fun `returns error when repository fails`() = runTest {
        repository.detailError = br.com.pokedex.core.common.result.DomainError.Network

        val result = useCase(1)

        assertEquals(Result.Error(br.com.pokedex.core.common.result.DomainError.Network), result)
    }

    @Test
    fun `delegates correct id to repository`() = runTest {
        repository.pokemonDetail = FakePokemonData.charmander

        val result = useCase(4)

        assertEquals(Result.Success(FakePokemonData.charmander), result)
    }
}
