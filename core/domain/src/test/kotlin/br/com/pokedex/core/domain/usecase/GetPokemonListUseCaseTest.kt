package br.com.pokedex.core.domain.usecase

import br.com.pokedex.core.testing.fake.FakePokemonRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPokemonListUseCaseTest {

    private lateinit var repository: FakePokemonRepository
    private lateinit var useCase: GetPokemonListUseCase

    @BeforeEach
    fun setUp() {
        repository = FakePokemonRepository()
        useCase = GetPokemonListUseCase(repository)
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        val flow = useCase()
        val pagingData = flow.first()
        assertNotNull(pagingData)
    }

    @Test
    fun `invoke delegates to repository getPokemonList`() = runTest {
        val useCaseFlow = useCase()
        val repoFlow = repository.getPokemonList()
        assertNotNull(useCaseFlow)
        assertNotNull(repoFlow)
    }
}
