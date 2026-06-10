package br.com.pokedex.feature.pokemoncompare.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import br.com.pokedex.core.route.keys.AppNavigator
import br.com.pokedex.core.route.keys.PokemonCompareKey
import br.com.pokedex.feature.pokemoncompare.ui.screen.PokemonCompareScreen

fun EntryProviderScope<NavKey>.pokemonCompareEntry(navigator: AppNavigator) {
    entry<PokemonCompareKey> { key ->
        PokemonCompareScreen(
            firstId = key.firstId,
            secondId = key.secondId,
            onBack = { navigator.navigateBack() },
        )
    }
}
