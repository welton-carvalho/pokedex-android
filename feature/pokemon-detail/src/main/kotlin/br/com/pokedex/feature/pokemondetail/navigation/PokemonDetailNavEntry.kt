package br.com.pokedex.feature.pokemondetail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import br.com.pokedex.core.route.keys.AppNavigator
import br.com.pokedex.core.route.keys.PokemonDetailKey
import br.com.pokedex.feature.pokemondetail.ui.screen.PokemonDetailScreen

fun EntryProviderScope<NavKey>.pokemonDetailEntry(navigator: AppNavigator) {
    entry<PokemonDetailKey> { key ->
        PokemonDetailScreen(
            pokemonId = key.pokemonId,
            onBack = { navigator.navigateBack() },
            onNavigateToNext = { nextId ->
                navigator.navigateBack()
                navigator.navigateTo(PokemonDetailKey(nextId))
            }
        )
    }
}
