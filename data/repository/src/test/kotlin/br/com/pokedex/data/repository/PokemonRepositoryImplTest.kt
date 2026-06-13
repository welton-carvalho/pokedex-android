package br.com.pokedex.data.repository

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.testing.fake.FakePokemonData
import br.com.pokedex.data.local.source.LocalPokemonDataSource
import br.com.pokedex.data.network.dto.AbilityNameDto
import br.com.pokedex.data.network.dto.AbilitySlotDto
import br.com.pokedex.data.network.dto.PokemonDetailDto
import br.com.pokedex.data.network.dto.StatDto
import br.com.pokedex.data.network.dto.StatNameDto
import br.com.pokedex.data.network.dto.TypeDto
import br.com.pokedex.data.network.dto.TypeSlotDto
import br.com.pokedex.data.network.source.RemotePokemonDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PokemonRepositoryImplTest {

    private lateinit var remote: RemotePokemonDataSource
    private lateinit var local: LocalPokemonDataSource
    private lateinit var repository: PokemonRepositoryImpl

    private val bulbasaurDto = PokemonDetailDto(
        id = 1,
        name = "bulbasaur",
        height = 7,
        weight = 69,
        types = listOf(TypeSlotDto(1, TypeDto("grass", "")), TypeSlotDto(2, TypeDto("poison", ""))),
        stats = listOf(
            StatDto(45, StatNameDto("hp", "")),
            StatDto(49, StatNameDto("attack", "")),
            StatDto(49, StatNameDto("defense", "")),
            StatDto(65, StatNameDto("special-attack", "")),
            StatDto(65, StatNameDto("special-defense", "")),
            StatDto(45, StatNameDto("speed", "")),
        ),
        abilities = listOf(
            AbilitySlotDto(false, 1, AbilityNameDto("overgrow", "")),
            AbilitySlotDto(true, 2, AbilityNameDto("chlorophyll", "")),
        ),
    )

    @BeforeEach
    fun setUp() {
        remote = mockk()
        local = mockk(relaxed = true)
        repository = PokemonRepositoryImpl(remote, local)
    }

    @Test
    fun `getPokemonDetail returns success and caches result`() = runTest {
        coEvery { remote.getPokemonDetail(1) } returns Result.Success(bulbasaurDto)
        coEvery { remote.getPokemonSpecies(any()) } returns Result.Error(DomainError.Network)
        coEvery { local.getPokemonDetail(1) } returns null

        val result = repository.getPokemonDetail(1)

        assertTrue(result is Result.Success)
        assertEquals("bulbasaur", (result as Result.Success).data.name)
        coVerify { local.savePokemonDetail(FakePokemonData.bulbasaur) }
    }

    @Test
    fun `getPokemonDetail falls back to cache on network error`() = runTest {
        coEvery { local.getPokemonDetail(1) } returns FakePokemonData.bulbasaur
        coEvery { remote.getPokemonDetail(1) } returns Result.Error(DomainError.Network)

        val result = repository.getPokemonDetail(1)

        assertTrue(result is Result.Success)
        assertEquals(FakePokemonData.bulbasaur, (result as Result.Success).data)
    }

    @Test
    fun `getPokemonDetail returns error when network fails and cache is empty`() = runTest {
        coEvery { remote.getPokemonDetail(99) } returns Result.Error(DomainError.Network)
        coEvery { local.getPokemonDetail(99) } returns null

        val result = repository.getPokemonDetail(99)

        assertTrue(result is Result.Error)
        assertEquals(DomainError.Network, (result as Result.Error).error)
    }

    @Test
    fun `getPokemonDetail calls remote exactly once`() = runTest {
        coEvery { remote.getPokemonDetail(1) } returns Result.Success(bulbasaurDto)
        coEvery { remote.getPokemonSpecies(any()) } returns Result.Error(DomainError.Network)
        coEvery { local.getPokemonDetail(1) } returns null

        repository.getPokemonDetail(1)

        coVerify(exactly = 1) { remote.getPokemonDetail(1) }
    }

    @Test
    fun `getPokemonList returns pager flow`() {
        val flow = repository.getPokemonList()
        assertTrue(flow != null)
    }
}
