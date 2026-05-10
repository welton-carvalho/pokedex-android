package br.com.pokedex.data.repository.di

import br.com.pokedex.core.domain.repository.PokemonRepository
import br.com.pokedex.data.repository.PokemonRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<PokemonRepository> { PokemonRepositoryImpl(get(), get()) }
}
