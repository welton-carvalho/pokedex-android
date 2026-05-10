package br.com.pokedex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import br.com.pokedex.core.designsystem.theme.PokedexLabTheme
import br.com.pokedex.core.navigation.PokemonDetailRoute
import br.com.pokedex.core.navigation.PokemonListRoute
import br.com.pokedex.feature.pokemondetail.ui.screen.PokemonDetailScreen
import br.com.pokedex.feature.pokemonlist.ui.screen.PokemonListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokedexLabTheme {
                val backStack = remember { mutableStateListOf<Any>(PokemonListRoute) }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            is PokemonListRoute -> NavEntry(key) {
                                PokemonListScreen(
                                    onNavigateToDetail = { id -> backStack.add(PokemonDetailRoute(id)) }
                                )
                            }
                            is PokemonDetailRoute -> NavEntry(key) {
                                PokemonDetailScreen(
                                    pokemonId = key.pokemonId,
                                    onBack = { backStack.removeLastOrNull() },
                                    onNavigateToNext = { nextId -> backStack.add(PokemonDetailRoute(nextId)) },
                                )
                            }
                            else -> error("Unknown route: $key")
                        }
                    }
                )
            }
        }
    }
}
