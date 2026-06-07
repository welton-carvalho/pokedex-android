package br.com.pokedex

import android.app.Application
import io.objectbox.android.Admin
import br.com.pokedex.core.common.di.commonModule
import br.com.pokedex.core.domain.di.domainModule
import br.com.pokedex.core.observability.AppLogger
import br.com.pokedex.data.local.di.localModule
import br.com.pokedex.data.local.entity.MyObjectBox
import br.com.pokedex.data.network.di.networkModule
import br.com.pokedex.data.repository.di.repositoryModule
import br.com.pokedex.feature.pokemoncompare.di.pokemonCompareModule
import br.com.pokedex.feature.pokemondetail.di.pokemonDetailModule
import br.com.pokedex.feature.pokemonlist.di.pokemonListModule
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import io.objectbox.BoxStore
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class PokedexLabApplication : Application() {

    lateinit var boxStore: BoxStore
        private set

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(BuildConfig.DEBUG)
        boxStore = MyObjectBox.builder().androidContext(this).build()
        if (BuildConfig.DEBUG) Admin(boxStore).start(this)
        initCoil()
        startKoin {
            androidContext(this@PokedexLabApplication)
            modules(
                // Provide the BoxStore instance so localModule can resolve it
                module { single { boxStore } },
                commonModule,
                domainModule,
                networkModule,
                localModule,
                repositoryModule,
                pokemonListModule,
                pokemonDetailModule,
                pokemonCompareModule,
            )
        }
    }

    private fun initCoil() {
        SingletonImageLoader.setSafe {
            ImageLoader.Builder(this)
                .memoryCache {
                    MemoryCache.Builder()
                        .maxSizePercent(this@PokedexLabApplication, 0.20)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDir.resolve("image_cache").toOkioPath())
                        .maxSizeBytes(50L * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
        }
    }
}
