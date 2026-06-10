package br.com.pokedex.feature.pokemoncompare.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.component.ErrorContent
import br.com.pokedex.core.designsystem.component.LoadingIndicator
import br.com.pokedex.core.designsystem.component.PokemonTypeChip
import br.com.pokedex.core.designsystem.theme.Body3Regular
import br.com.pokedex.core.designsystem.theme.Gray2
import br.com.pokedex.core.designsystem.theme.Subtitle1Bold
import br.com.pokedex.core.designsystem.theme.Subtitle2Bold
import br.com.pokedex.feature.pokemoncompare.ui.model.ComparePokemonUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatWinner
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.SideUiState
import coil3.compose.AsyncImage

/**
 * Renderiza uma coluna da comparação conforme seu [SideUiState] (independente da outra coluna).
 * O destaque do maior por stat só é aplicado quando [winnerByLabel] é não-nulo (ambos carregaram).
 */
@Composable
fun CompareColumn(
    side: CompareSide,
    state: SideUiState,
    winnerByLabel: Map<String, StatWinner>?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        when (state) {
            is SideUiState.Loading ->
                Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                    LoadingIndicator()
                }

            is SideUiState.Error ->
                ErrorContent(
                    message = "Could not load. Please try again.",
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .testTag("compare-error-${side.name.lowercase()}"),
                )

            is SideUiState.Success ->
                CompareColumnContent(pokemon = state.pokemon, side = side, winnerByLabel = winnerByLabel)
        }
    }
}

@Composable
private fun CompareColumnContent(
    pokemon: ComparePokemonUiModel,
    side: CompareSide,
    winnerByLabel: Map<String, StatWinner>?,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            modifier = Modifier.size(120.dp),
        )
        Text(
            text = "#${pokemon.id.toString().padStart(3, '0')}",
            style = Body3Regular,
            color = Gray2,
        )
        Text(text = pokemon.name, style = Subtitle1Bold, color = pokemon.typeColor)

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            pokemon.types.forEach { type -> PokemonTypeChip(typeName = type) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // About
        Text(text = "Weight: ${pokemon.weight}", style = Body3Regular)
        Text(text = "Height: ${pokemon.height}", style = Body3Regular)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Abilities", style = Subtitle2Bold, color = pokemon.typeColor)
        pokemon.abilities.forEach { ability ->
            Text(text = ability, style = Body3Regular, textAlign = TextAlign.Center)
        }

        if (pokemon.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pokemon.description,
                style = Body3Regular,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Base Stats", style = Subtitle1Bold, color = pokemon.typeColor)
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            pokemon.stats.forEach { stat ->
                val isWinner = winnerByLabel?.get(stat.label) == when (side) {
                    CompareSide.FIRST -> StatWinner.FIRST
                    CompareSide.SECOND -> StatWinner.SECOND
                }
                StatComparisonRowItem(
                    stat = stat,
                    isWinner = isWinner,
                    baseColor = pokemon.typeColor,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
