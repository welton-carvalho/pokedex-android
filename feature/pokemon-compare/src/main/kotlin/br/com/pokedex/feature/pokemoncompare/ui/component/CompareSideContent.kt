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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.component.ErrorContent
import br.com.pokedex.core.designsystem.component.LoadingIndicator
import br.com.pokedex.core.designsystem.component.PokemonTypeChip
import br.com.pokedex.core.designsystem.theme.Body2Regular
import br.com.pokedex.core.designsystem.theme.Gray2
import br.com.pokedex.core.designsystem.theme.Subtitle1Bold
import br.com.pokedex.core.designsystem.theme.Subtitle2Bold
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel
import coil3.compose.AsyncImage

@Composable
internal fun CompareSideHeader(
    side: SideContent,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (side) {
            SideContent.Loading -> LoadingIndicator()
            is SideContent.Error -> ErrorContent(
                message = "Could not load Pokémon",
                onRetry = side.onRetry,
                modifier = Modifier.fillMaxWidth(),
            )
            is SideContent.Success -> CompareSideHero(side.data)
        }
    }
}

@Composable
private fun CompareSideHero(data: CompareUiModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        AsyncImage(
            model = data.imageUrl,
            contentDescription = data.name,
            modifier = Modifier.size(120.dp),
        )
        Text(
            text = data.formattedNumber,
            style = Subtitle2Bold,
            color = Gray2,
        )
        Text(
            text = data.name,
            style = Subtitle1Bold,
            color = data.primaryTypeColor,
            textAlign = TextAlign.Center,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            data.types.forEach { type ->
                PokemonTypeChip(typeName = type)
            }
        }
    }
}

@Composable
internal fun CompareSideMeasures(
    data: CompareUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        MeasureItem(label = "Height", value = "${data.heightMeters} m")
        Spacer(modifier = Modifier.size(16.dp))
        MeasureItem(label = "Weight", value = "${data.weightKg} kg")
    }
}

@Composable
private fun MeasureItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = Subtitle2Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, style = Body2Regular, color = Gray2)
    }
}

internal sealed interface SideContent {
    data object Loading : SideContent
    data class Error(val onRetry: () -> Unit) : SideContent
    data class Success(val data: CompareUiModel) : SideContent
}
