package br.com.pokedex.feature.pokemonlist.di

import br.com.pokedex.core.domain.usecase.GetPokemonListUseCase
import br.com.pokedex.feature.pokemonlist.viewmodel.PokemonListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pokemonListModule = module {
    factory { GetPokemonListUseCase(get()) }
    viewModel { PokemonListViewModel(get()) }
}
