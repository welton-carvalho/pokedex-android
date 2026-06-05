package br.com.pokedex.feature.pokemoncompare.di

import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.feature.pokemoncompare.viewmodel.CompareViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val compareModule = module {
    factory { GetPokemonDetailUseCase(get()) }
    viewModel { (idA: Int, idB: Int) ->
        CompareViewModel(
            idA = idA,
            idB = idB,
            getPokemonDetail = get(),
            dispatcherProvider = get(),
        )
    }
}
