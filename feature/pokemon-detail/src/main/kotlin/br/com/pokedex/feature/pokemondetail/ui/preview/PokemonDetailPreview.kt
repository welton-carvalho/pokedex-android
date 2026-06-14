package br.com.pokedex.feature.pokemondetail.ui.preview

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import br.com.pokedex.core.designsystem.theme.PokedexLabTheme
import br.com.pokedex.core.designsystem.theme.TypeGrass
import br.com.pokedex.feature.pokemondetail.R
import br.com.pokedex.feature.pokemondetail.ui.model.PokemonDetailUiModel
import br.com.pokedex.feature.pokemondetail.ui.model.StatUiModel
import br.com.pokedex.feature.pokemondetail.ui.screen.PokemonDetailContent
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

private val bulbasaurPreview = PokemonDetailUiModel(
    id = 1,
    name = "Bulbasaur",
    imageUrl = "",
    typeColor = TypeGrass,
    types = listOf("grass", "poison"),
    height = "0.7m",
    weight = "6.9kg",
    abilities = listOf("Overgrow", "Chlorophyll"),
    stats = listOf(
        StatUiModel(label = "HP", value = 45),
        StatUiModel(label = "ATK", value = 49),
        StatUiModel(label = "DEF", value = 45),
        StatUiModel(label = "SATK", value = 65),
        StatUiModel(label = "SDEF", value = 65),
        StatUiModel(label = "SPD", value = 45),
    ),
    description = "A strange seed was planted on its back at birth. The plant sprouts and grows with this Pokémon.",
)

@Preview(showSystemUi = true)
@Composable
private fun PokemonDetailContentPreview() {
    val context = LocalContext.current
    PokedexLabTheme {
        CompositionLocalProvider(
            LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler { _ ->
                BitmapFactory.decodeResource(context.resources, R.drawable.placeholder).asImage()
            },
        ) {
            PokemonDetailContent(
                pokemon = bulbasaurPreview,
                onBack = {},
                onNavigateToNext = {},
            )
        }
    }
}
