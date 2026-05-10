package br.com.pokedex

import android.app.Application
import br.com.pokedex.core.common.di.commonModule
import br.com.pokedex.core.observability.AppLogger
import br.com.pokedex.data.local.di.localModule
import br.com.pokedex.data.network.di.networkModule
import br.com.pokedex.data.repository.di.repositoryModule
import br.com.pokedex.feature.pokemondetail.di.pokemonDetailModule
import br.com.pokedex.feature.pokemonlist.di.pokemonListModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PokedexLabApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.init(BuildConfig.DEBUG)
        startKoin {
            androidContext(this@PokedexLabApplication)
            modules(
                commonModule,
                networkModule,
                localModule,
                repositoryModule,
                pokemonListModule,
                pokemonDetailModule,
            )
        }
    }
}
