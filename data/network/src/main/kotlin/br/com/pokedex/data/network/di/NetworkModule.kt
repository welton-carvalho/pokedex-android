package br.com.pokedex.data.network.di

import br.com.pokedex.data.network.api.PokemonApiService
import br.com.pokedex.data.network.source.RemotePokemonDataSource
import com.chuckerteam.chucker.api.ChuckerInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://pokeapi.co/api/v2/"
private const val TIMEOUT_SECONDS = 30L

val networkModule = module {
    single {
        ChuckerInterceptor.Builder(androidContext()).build()
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<ChuckerInterceptor>())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    single {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(get())
            .build()
    }

    single<PokemonApiService> { get<Retrofit>().create(PokemonApiService::class.java) }

    single { RemotePokemonDataSource(get()) }
}
