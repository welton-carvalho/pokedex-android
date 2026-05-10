package br.com.pokedex.core.common.di

import br.com.pokedex.core.common.dispatcher.DefaultDispatcherProvider
import br.com.pokedex.core.common.dispatcher.DispatcherProvider
import org.koin.dsl.module

val commonModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}
