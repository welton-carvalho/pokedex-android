package br.com.pokedex.feature.pokemoncompare.viewmodel

import br.com.pokedex.core.common.result.AsyncResult
import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.core.common.result.Result
import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.core.testing.dispatcher.TestDispatcherProvider
import br.com.pokedex.core.testing.fake.FakePokemonData
import br.com.pokedex.core.testing.rule.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CompareViewModelTest {

    @JvmField
    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val dispatcherProvider = TestDispatcherProvider(mainDispatcherRule.dispatcher)

    private fun createViewModel(
        idA: Int,
        idB: Int,
        useCase: GetPokemonDetailUseCase = mockk(relaxed = true),
    ): CompareViewModel = CompareViewModel(
        idA = idA,
        idB = idB,
        getPokemonDetail = useCase,
        dispatcherProvider = dispatcherProvider,
    )

    @Test
    fun `both sides load successfully in parallel`() = runTest {
        val useCase: GetPokemonDetailUseCase = mockk()
        coEvery { useCase(1) } returns Result.Success(FakePokemonData.bulbasaur)
        coEvery { useCase(4) } returns Result.Success(FakePokemonData.charmander)

        val viewModel = createViewModel(idA = 1, idB = 4, useCase = useCase)

        val state = viewModel.state.value
        assertFalse(state.isInvalidInput)
        assertTrue(state.pokemonA is AsyncResult.Success)
        assertTrue(state.pokemonB is AsyncResult.Success)
        assertEquals(1, (state.pokemonA as AsyncResult.Success).data.id)
        assertEquals(4, (state.pokemonB as AsyncResult.Success).data.id)
    }

    @Test
    fun `same id triggers isInvalidInput and no network calls`() = runTest {
        val useCase: GetPokemonDetailUseCase = mockk()

        val viewModel = createViewModel(idA = 1, idB = 1, useCase = useCase)

        val state = viewModel.state.value
        assertTrue(state.isInvalidInput)
        assertEquals(AsyncResult.Loading, state.pokemonA)
        assertEquals(AsyncResult.Loading, state.pokemonB)
    }

    @Test
    fun `error on side A keeps side B success`() = runTest {
        val useCase: GetPokemonDetailUseCase = mockk()
        coEvery { useCase(1) } returns Result.Error(DomainError.Network)
        coEvery { useCase(4) } returns Result.Success(FakePokemonData.charmander)

        val viewModel = createViewModel(idA = 1, idB = 4, useCase = useCase)

        val state = viewModel.state.value
        assertTrue(state.pokemonA is AsyncResult.Error)
        assertEquals(DomainError.Network, (state.pokemonA as AsyncResult.Error).error)
        assertTrue(state.pokemonB is AsyncResult.Success)
    }

    @Test
    fun `unexpected exception on side B is converted via ErrorHandler`() = runTest {
        val useCase: GetPokemonDetailUseCase = mockk()
        coEvery { useCase(1) } returns Result.Success(FakePokemonData.bulbasaur)
        coEvery { useCase(4) } throws RuntimeException("boom")

        val viewModel = createViewModel(idA = 1, idB = 4, useCase = useCase)

        val state = viewModel.state.value
        assertTrue(state.pokemonA is AsyncResult.Success)
        assertTrue(state.pokemonB is AsyncResult.Error)
        assertEquals(DomainError.Unknown, (state.pokemonB as AsyncResult.Error).error)
    }

    @Test
    fun `retry side A recovers from error`() = runTest {
        val useCase: GetPokemonDetailUseCase = mockk()
        var attemptsForA = 0
        coEvery { useCase(1) } answers {
            attemptsForA += 1
            if (attemptsForA == 1) Result.Error(DomainError.Network)
            else Result.Success(FakePokemonData.bulbasaur)
        }
        coEvery { useCase(4) } returns Result.Success(FakePokemonData.charmander)

        val viewModel = createViewModel(idA = 1, idB = 4, useCase = useCase)
        assertTrue(viewModel.state.value.pokemonA is AsyncResult.Error)

        viewModel.onIntent(
            br.com.pokedex.feature.pokemoncompare.ui.intent.CompareIntent.RetryPokemon(
                br.com.pokedex.feature.pokemoncompare.ui.intent.Side.A,
            ),
        )

        val state = viewModel.state.value
        assertTrue(state.pokemonA is AsyncResult.Success)
        assertEquals(2, attemptsForA)
    }
}
