package br.com.pokedex.core.domain.di

import br.com.pokedex.core.domain.usecase.GetPokemonDetailUseCase
import br.com.pokedex.core.domain.usecase.GetPokemonListUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetPokemonListUseCase(get()) }
    factory { GetPokemonDetailUseCase(get()) }
}
