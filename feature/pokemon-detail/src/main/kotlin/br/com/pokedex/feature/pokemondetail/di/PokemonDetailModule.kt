package br.com.pokedex.feature.pokemondetail.di

import br.com.pokedex.feature.pokemondetail.viewmodel.PokemonDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pokemonDetailModule = module {
    viewModel { (pokemonId: Int) -> PokemonDetailViewModel(get(), pokemonId) }
}
