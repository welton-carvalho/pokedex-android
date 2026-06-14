package br.com.pokedex.appfunctions

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import br.com.pokedex.MainActivity
import br.com.pokedex.data.network.source.RemotePokemonDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * AppFunctions that expose Pokémon browsing to Android AI agents.
 *
 * Required workflow: call [listPokemons] first to obtain valid pokemonId values,
 * then call [openPokemonDetail] with the chosen id.
 */
class PokemonAppFunctions : KoinComponent {

    private val remoteDataSource: RemotePokemonDataSource by inject()

    /**
     * Lists the first 50 Pokémons available in the Pokédex.
     *
     * @param appFunctionContext The execution context.
     * @return A list of [PokemonItem] objects each containing an id and display name.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun listPokemons(appFunctionContext: AppFunctionContext): List<PokemonItem> =
        withContext(Dispatchers.IO) {
            remoteDataSource.getPokemonList(offset = 0, limit = 50)
                .results
                .map { dto -> PokemonItem(id = dto.extractId(), name = dto.name) }
        }

    /**
     * Opens the Pokémon detail screen for the selected Pokémon.
     * Required workflow: Call [listPokemons] first to obtain a valid pokemonId.
     *
     * @param appFunctionContext The execution context.
     * @param pokemonId The numeric ID of the Pokémon to display, as returned by [listPokemons].
     * @return A [PendingIntent] that navigates the user to the Pokémon detail screen in the app.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun openPokemonDetail(
        appFunctionContext: AppFunctionContext,
        pokemonId: Int,
    ): PendingIntent = withContext(Dispatchers.IO) {
        val deepLink = Uri.parse("pokedex://pokemon/$pokemonId")
        val intent = Intent(Intent.ACTION_VIEW, deepLink).apply {
            setClass(appFunctionContext.context, MainActivity::class.java)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        PendingIntent.getActivity(
            appFunctionContext.context,
            pokemonId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

/**
 * A Pokémon entry returned by the Pokédex listing.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class PokemonItem(
    /** The Pokémon's numeric identifier */
    val id: Int,
    /** The Pokémon's display name */
    val name: String,
)
