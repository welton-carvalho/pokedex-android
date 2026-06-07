package br.com.pokedex.feature.pokemoncompare.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.pokedex.core.designsystem.component.ErrorContent
import br.com.pokedex.core.designsystem.component.LoadingIndicator
import br.com.pokedex.core.designsystem.component.PokemonTypeChip
import br.com.pokedex.core.designsystem.theme.Body2Regular
import br.com.pokedex.core.designsystem.theme.Gray3
import br.com.pokedex.core.designsystem.theme.HeadlineBold
import br.com.pokedex.core.designsystem.theme.PokedexRed
import br.com.pokedex.core.designsystem.theme.Subtitle2Bold
import br.com.pokedex.core.designsystem.theme.Subtitle3Bold
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.core.ui.PokemonStatBar
import br.com.pokedex.feature.pokemoncompare.mapper.buildStatComparison
import br.com.pokedex.feature.pokemoncompare.ui.event.CompareEvent
import br.com.pokedex.feature.pokemoncompare.ui.intent.CompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.model.CompareUiModel
import br.com.pokedex.feature.pokemoncompare.ui.model.StatComparisonRow
import br.com.pokedex.feature.pokemoncompare.viewmodel.CompareViewModel
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CompareScreen(
    firstId: Int,
    secondId: Int,
    onBack: () -> Unit,
    viewModel: CompareViewModel = koinViewModel(
        key = "compare_${firstId}_${secondId}",
        parameters = { parametersOf(firstId, secondId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CompareEvent.NavigateBack -> onBack()
            }
        }
    }

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        }
        state.error != null -> {
            ErrorContent(
                message = "Could not load Pokémon. Please try again.",
                onRetry = { viewModel.onIntent(CompareIntent.Retry) },
            )
        }
        state.first != null && state.second != null -> {
            CompareContent(
                first = state.first!!,
                second = state.second!!,
                onBack = { viewModel.onIntent(CompareIntent.NavigateBack) },
            )
        }
    }
}

@Composable
private fun CompareContent(
    first: CompareUiModel,
    second: CompareUiModel,
    onBack: () -> Unit,
) {
    val comparison = buildStatComparison(first.stats, second.stats)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PokedexRed),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .statusBarsPadding()
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
                modifier = Modifier.weight(1f),
            )
        }

        // White content card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = White,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                )
                .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Artwork row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                PokemonColumn(pokemon = first)
                VerticalDivider(
                    modifier = Modifier
                        .height(160.dp)
                        .align(Alignment.CenterVertically),
                    thickness = 1.dp,
                    color = Gray3,
                )
                PokemonColumn(pokemon = second)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Base Stats header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(
                    text = "Base Stats",
                    style = Subtitle2Bold,
                    color = first.typeColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Base Stats",
                    style = Subtitle2Bold,
                    color = second.typeColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats side by side
            comparison.forEach { row ->
                CompareStatRow(
                    row = row,
                    firstColor = if (row.firstWins) first.typeColor else Gray3,
                    secondColor = if (row.secondWins) second.typeColor else Gray3,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PokemonColumn(
    pokemon: CompareUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            modifier = Modifier.size(96.dp),
        )
        Text(text = pokemon.number, style = Body2Regular, color = Gray3)
        Text(text = pokemon.name, style = Subtitle2Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            pokemon.types.forEach { type ->
                PokemonTypeChip(typeName = type)
            }
        }
    }
}

@Composable
private fun CompareStatRow(
    row: StatComparisonRow,
    firstColor: androidx.compose.ui.graphics.Color,
    secondColor: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PokemonStatBar(
            label = row.label,
            value = row.firstValue,
            color = firstColor,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = row.label,
            style = Subtitle3Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Reversed stat bar for second pokemon (mirror layout)
        PokemonStatBar(
            label = row.secondValue.toString().padStart(3, '0'),
            value = row.secondValue,
            color = secondColor,
            modifier = Modifier.weight(1f),
        )
    }
}
