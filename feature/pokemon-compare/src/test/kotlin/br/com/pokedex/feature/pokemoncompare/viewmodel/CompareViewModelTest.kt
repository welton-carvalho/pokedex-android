package br.com.pokedex.feature.pokemoncompare.viewmodel

import br.com.pokedex.core.common.result.DomainError
import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.core.testing.fake.FakePokemonData
import br.com.pokedex.core.testing.fake.FakePokemonRepository
import br.com.pokedex.core.testing.rule.MainDispatcherRule
import br.com.pokedex.feature.pokemoncompare.ui.intent.CompareIntent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class CompareViewModelTest {

    @RegisterExtension
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePokemonRepository()
    private val useCase = GetPokemonDetailUseCase(repository)

    private fun viewModel() = CompareViewModel(useCase, firstId = 1, secondId = 4)

    @Test
    fun `both success produces success state`() {
        repository.pokemonDetail = FakePokemonData.bulbasaur
        val vm = viewModel()
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.first)
        assertNotNull(state.second)
        assertNull(state.error)
    }

    @Test
    fun `first error produces error state`() {
        repository.detailError = DomainError.Network
        val vm = viewModel()
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertEquals(DomainError.Network, state.error)
    }

    @Test
    fun `Retry intent reloads both pokemon`() {
        repository.detailError = DomainError.Network
        val vm = viewModel()
        assertTrue(vm.state.value.error != null)

        repository.detailError = null
        repository.pokemonDetail = FakePokemonData.bulbasaur
        vm.onIntent(CompareIntent.Retry)

        val state = vm.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.first)
        assertNull(state.error)
    }
}
