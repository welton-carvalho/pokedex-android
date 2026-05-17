package br.com.pokedex.core.route.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import br.com.pokedex.core.route.keys.AppNavigator

internal class NavBackStackNavigator(
    private val backStack: NavBackStack<NavKey>
) : AppNavigator {
    override fun navigateTo(key: NavKey) { backStack.add(key) }
    override fun navigateBack() { backStack.removeLastOrNull() }
}
