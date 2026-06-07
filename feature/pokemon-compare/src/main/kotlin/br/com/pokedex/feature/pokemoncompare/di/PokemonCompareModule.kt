package br.com.pokedex.feature.pokemoncompare.di

import br.com.pokedex.feature.pokemoncompare.viewmodel.CompareViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pokemonCompareModule = module {
    viewModel { (firstId: Int, secondId: Int) -> CompareViewModel(get(), firstId, secondId) }
}
