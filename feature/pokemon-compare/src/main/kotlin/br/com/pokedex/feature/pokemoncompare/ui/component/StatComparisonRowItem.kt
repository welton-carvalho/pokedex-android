package br.com.pokedex.feature.pokemoncompare.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.pokedex.core.ui.PokemonStatBar
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareStatUiModel

/** Cor de destaque para o maior valor de um stat entre os dois Pokémon. */
val StatWinnerColor = Color(0xFF4CAF50)

@Composable
fun StatComparisonRowItem(
    stat: CompareStatUiModel,
    isWinner: Boolean,
    baseColor: Color,
    modifier: Modifier = Modifier,
) {
    PokemonStatBar(
        label = stat.label,
        value = stat.value,
        color = if (isWinner) StatWinnerColor else baseColor,
        modifier = modifier.fillMaxWidth(),
    )
}
