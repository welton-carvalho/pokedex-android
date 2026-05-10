package br.com.pokedex.feature.pokemondetail.di

import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.feature.pokemondetail.viewmodel.PokemonDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pokemonDetailModule = module {
    factory { GetPokemonDetailUseCase(get()) }
    viewModel { (pokemonId: Int) -> PokemonDetailViewModel(get(), pokemonId) }
}
