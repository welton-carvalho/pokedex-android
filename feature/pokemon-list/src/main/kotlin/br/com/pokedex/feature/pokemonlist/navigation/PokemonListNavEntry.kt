package br.com.pokedex.feature.pokemonlist.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import br.com.pokedex.core.route.keys.AppNavigator
import br.com.pokedex.core.route.keys.PokemonCompareKey
import br.com.pokedex.core.route.keys.PokemonDetailKey
import br.com.pokedex.core.route.keys.PokemonListKey
import br.com.pokedex.feature.pokemonlist.ui.screen.PokemonListScreen

fun EntryProviderScope<NavKey>.pokemonListEntry(navigator: AppNavigator) {
    entry<PokemonListKey> {
        PokemonListScreen(
            onNavigateToDetail = { id -> navigator.navigateTo(PokemonDetailKey(id)) },
            onNavigateToCompare = { first, second -> navigator.navigateTo(PokemonCompareKey(first, second)) },
        )
    }
}
