package br.com.pokedex.data.local.di

import br.com.pokedex.data.local.entity.PokemonDetailEntity
import br.com.pokedex.data.local.entity.PokemonSummaryEntity
import br.com.pokedex.data.local.source.LocalPokemonDataSource
import io.objectbox.BoxStore
import org.koin.dsl.module

// BoxStore is created in PokedexLabApplication (after MyObjectBox is generated)
// and declared into Koin before this module resolves.
val localModule = module {
    single {
        LocalPokemonDataSource(
            summaryBox = get<BoxStore>().boxFor(PokemonSummaryEntity::class.java),
            detailBox = get<BoxStore>().boxFor(PokemonDetailEntity::class.java),
        )
    }
}
