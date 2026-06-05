package br.com.pokedex.feature.pokemoncompare.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.pokedex.core.common.result.AsyncResult
import br.com.pokedex.core.designsystem.theme.Body2Regular
import br.com.pokedex.core.designsystem.theme.Gray1
import br.com.pokedex.core.designsystem.theme.GrayBackground
import br.com.pokedex.core.designsystem.theme.HeadlineBold
import br.com.pokedex.core.designsystem.theme.PokedexRed
import br.com.pokedex.core.designsystem.theme.Subtitle1Bold
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.core.ui.CompareStatRow
import br.com.pokedex.feature.pokemoncompare.ui.component.CompareSideHeader
import br.com.pokedex.feature.pokemoncompare.ui.component.CompareSideMeasures
import br.com.pokedex.feature.pokemoncompare.ui.component.SideContent
import br.com.pokedex.feature.pokemoncompare.ui.intent.CompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.intent.Side
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareState
import br.com.pokedex.feature.pokemoncompare.viewmodel.CompareViewModel

@Composable
fun CompareScreen(
    onBack: () -> Unit,
    viewModel: CompareViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CompareScreenContent(
        state = state,
        onBack = onBack,
        onRetry = { side -> viewModel.onIntent(CompareIntent.RetryPokemon(side)) },
    )
}

@Composable
internal fun CompareScreenContent(
    state: CompareState,
    onBack: () -> Unit,
    onRetry: (Side) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokedexRed),
    ) {
        CompareHeader(onBack = onBack, modifier = Modifier.statusBarsPadding())
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = GrayBackground,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                ),
        ) {
            if (state.isInvalidInput) {
                InvalidInputContent(modifier = Modifier.fillMaxSize().testTag("compare_invalid_input"))
            } else {
                CompareBody(
                    state = state,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun CompareHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = White,
            )
        }
        Text(
            text = "Compare",
            style = HeadlineBold,
            color = White,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun InvalidInputContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text(text = "Invalid comparison", style = Subtitle1Bold, color = Gray1)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pick two different Pokémon to compare.",
                style = Body2Regular,
                color = Gray1,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CompareBody(
    state: CompareState,
    onRetry: (Side) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f).testTag("compare_side_A")) {
                CompareSideHeader(side = state.pokemonA.toSideContent { onRetry(Side.A) })
            }
            Spacer(modifier = Modifier.size(8.dp))
            Box(modifier = Modifier.weight(1f).testTag("compare_side_B")) {
                CompareSideHeader(side = state.pokemonB.toSideContent { onRetry(Side.B) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.pokemonA is AsyncResult.Success && state.pokemonB is AsyncResult.Success) {
            val dataA = state.pokemonA.data
            val dataB = state.pokemonB.data
            MeasuresBlock(dataA, dataB)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Base Stats",
                style = Subtitle1Bold,
                color = Gray1,
                modifier = Modifier.padding(start = 4.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            StatsBlock(dataA, dataB)
        }
    }
}

@Composable
private fun MeasuresBlock(dataA: CompareUiModel, dataB: CompareUiModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) { CompareSideMeasures(dataA) }
        Spacer(modifier = Modifier.size(8.dp))
        Box(modifier = Modifier.weight(1f)) { CompareSideMeasures(dataB) }
    }
}

@Composable
private fun StatsBlock(dataA: CompareUiModel, dataB: CompareUiModel) {
    val stats = dataA.stats.zip(dataB.stats)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEach { (statA, statB) ->
            CompareStatRow(
                label = statA.label,
                valueA = statA.value,
                valueB = statB.value,
                colorA = dataA.primaryTypeColor,
                colorB = dataB.primaryTypeColor,
                maxValue = statA.maxValue,
            )
        }
    }
}

private fun AsyncResult<CompareUiModel>.toSideContent(onRetry: () -> Unit): SideContent = when (this) {
    AsyncResult.Loading -> SideContent.Loading
    is AsyncResult.Error -> SideContent.Error(onRetry = onRetry)
    is AsyncResult.Success -> SideContent.Success(data)
}
