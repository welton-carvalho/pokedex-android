package br.com.pokedex.feature.pokemoncompare.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import br.com.pokedex.core.route.keys.AppNavigator
import br.com.pokedex.core.route.keys.PokemonCompareKey
import br.com.pokedex.feature.pokemoncompare.ui.screen.CompareScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.pokemonCompareEntry(navigator: AppNavigator) {
    entry<PokemonCompareKey> { key ->
        CompareScreen(
            onBack = { navigator.navigateBack() },
            viewModel = koinViewModel(
                key = "pokemon_compare_${key.idA}_${key.idB}",
                parameters = { parametersOf(key.idA, key.idB) },
            ),
        )
    }
}
