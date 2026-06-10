package br.com.pokedex.feature.pokemoncompare.ui.preview

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import br.com.pokedex.core.designsystem.theme.PokedexLabTheme
import br.com.pokedex.feature.pokemoncompare.comparison.buildStatComparison
import br.com.pokedex.feature.pokemoncompare.ui.component.CompareColumn
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareStatUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.SideUiState

private val bulbasaur = ComparePokemonUiModel(
    id = 1,
    name = "Bulbasaur",
    imageUrl = "",
    typeColor = Color(0xFF63BC5A),
    types = listOf("grass", "poison"),
    height = "0.7m",
    weight = "6.9kg",
    abilities = listOf("Overgrow"),
    description = "A strange seed was planted on its back at birth.",
    stats = listOf(
        CompareStatUiModel("HP", 45),
        CompareStatUiModel("ATK", 49),
        CompareStatUiModel("DEF", 49),
        CompareStatUiModel("SPD", 45),
    ),
)

private val charmander = ComparePokemonUiModel(
    id = 4,
    name = "Charmander",
    imageUrl = "",
    typeColor = Color(0xFFFF9741),
    types = listOf("fire"),
    height = "0.6m",
    weight = "8.5kg",
    abilities = listOf("Blaze"),
    description = "It has a preference for hot things.",
    stats = listOf(
        CompareStatUiModel("HP", 39),
        CompareStatUiModel("ATK", 52),
        CompareStatUiModel("DEF", 43),
        CompareStatUiModel("SPD", 65),
    ),
)

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun CompareColumnsPreview() {
    val winners = buildStatComparison(bulbasaur, charmander).associate { it.label to it.winner }
    PokedexLabTheme {
        Row(modifier = Modifier.fillMaxWidth()) {
            CompareColumn(
                side = CompareSide.FIRST,
                state = SideUiState.Success(bulbasaur),
                winnerByLabel = winners,
                onRetry = {},
                modifier = Modifier.weight(1f),
            )
            CompareColumn(
                side = CompareSide.SECOND,
                state = SideUiState.Success(charmander),
                winnerByLabel = winners,
                onRetry = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
