package br.com.pokedex.feature.pokemoncompare.di

import br.com.pokedex.feature.pokemoncompare.viewmodel.PokemonCompareViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val pokemonCompareModule = module {
    // GetPokemonDetailUseCase é resolvido do grafo Koin (registrado por pokemonDetailModule);
    // não é re-registrado aqui para evitar DefinitionOverrideException.
    viewModel { (firstId: Int, secondId: Int) ->
        PokemonCompareViewModel(get(), firstId, secondId)
    }
}
