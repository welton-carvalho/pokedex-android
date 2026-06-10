package br.com.pokedex.core.route.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.com.pokedex.core.route.deeplink.DeepLinkRouter
import br.com.pokedex.core.route.keys.PokemonListKey
import br.com.pokedex.feature.pokemoncompare.navigation.pokemonCompareEntry
import br.com.pokedex.feature.pokemondetail.navigation.pokemonDetailEntry
import br.com.pokedex.feature.pokemonlist.navigation.pokemonListEntry

@Composable
fun AppNavDisplay(initialIntent: Intent? = null) {
    val deepLinkRouter = remember { DeepLinkRouter() }
    val initialKeys = remember(initialIntent) {
        initialIntent?.let { deepLinkRouter.fromIntent(it) } ?: listOf(PokemonListKey)
    }
    val backStack = rememberNavBackStack(*initialKeys.toTypedArray())
    val navigator = remember(backStack) { NavBackStackNavigator(backStack) }

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.navigateBack() },
        entryProvider = entryProvider {
            pokemonListEntry(navigator)
            pokemonDetailEntry(navigator)
            pokemonCompareEntry(navigator)
        }
    )
}
