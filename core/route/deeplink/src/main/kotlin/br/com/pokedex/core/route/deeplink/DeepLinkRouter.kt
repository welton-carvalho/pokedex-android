package br.com.pokedex.core.route.deeplink

import android.content.Intent
import androidx.navigation3.runtime.NavKey
import br.com.pokedex.core.route.keys.PokemonCompareKey
import br.com.pokedex.core.route.keys.PokemonDetailKey
import br.com.pokedex.core.route.keys.PokemonListKey

class DeepLinkRouter {

    /** Recebe um Intent externo do Android e retorna um backstack sintético, ou null se não for um deep link reconhecido. */
    fun fromIntent(intent: Intent): List<NavKey>? {
        val url = intent.data?.toString() ?: return null
        return fromUrl(url)?.let { key -> listOf(PokemonListKey, key) }
    }

    /** Converte uma URL de deep link para a key de navegação correspondente. */
    fun fromUrl(url: String): NavKey? = when {
        url == "pokedex://list" -> PokemonListKey
        url.startsWith("pokedex://pokemon/") -> {
            val id = url.removePrefix("pokedex://pokemon/").toIntOrNull()
            id?.let { PokemonDetailKey(it) }
        }
        url.startsWith("pokedex://compare/") -> {
            // pokedex://compare/{id1}/{id2} — valida ambos os ids (SECURITY-05)
            val parts = url.removePrefix("pokedex://compare/").split("/")
            val first = parts.getOrNull(0)?.toIntOrNull()
            val second = parts.getOrNull(1)?.toIntOrNull()
            if (first != null && first > 0 && second != null && second > 0) {
                PokemonCompareKey(first, second)
            } else {
                null
            }
        }
        else -> null
    }
}
