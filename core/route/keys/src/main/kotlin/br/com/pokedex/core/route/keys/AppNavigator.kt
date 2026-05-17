package br.com.pokedex.core.route.keys

import androidx.navigation3.runtime.NavKey

interface AppNavigator {
    fun navigateTo(key: NavKey)
    fun navigateBack()
}
