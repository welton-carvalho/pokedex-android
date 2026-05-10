package br.com.pokedex.data.local.di

import br.com.pokedex.data.local.source.LocalPokemonDataSource
import org.koin.dsl.module

val localModule = module {
    single { LocalPokemonDataSource() }
}
