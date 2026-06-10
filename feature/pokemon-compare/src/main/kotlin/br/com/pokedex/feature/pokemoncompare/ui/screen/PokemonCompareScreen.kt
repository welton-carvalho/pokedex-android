package br.com.pokedex.feature.pokemoncompare.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.pokedex.core.designsystem.theme.HeadlineBold
import br.com.pokedex.core.designsystem.theme.PokedexRed
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.feature.pokemoncompare.comparison.buildStatComparison
import br.com.pokedex.feature.pokemoncompare.ui.component.CompareColumn
import br.com.pokedex.feature.pokemoncompare.ui.event.PokemonCompareEvent
import br.com.pokedex.feature.pokemoncompare.ui.intent.PokemonCompareIntent
import br.com.pokedex.feature.pokemoncompare.ui.model.StatComparisonRow
import br.com.pokedex.feature.pokemoncompare.ui.state.CompareSide
import br.com.pokedex.feature.pokemoncompare.ui.state.SideUiState
import br.com.pokedex.feature.pokemoncompare.viewmodel.PokemonCompareViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PokemonCompareScreen(
    firstId: Int,
    secondId: Int,
    onBack: () -> Unit,
    viewModel: PokemonCompareViewModel = koinViewModel(
        key = "pokemon_compare_${firstId}_$secondId",
        parameters = { parametersOf(firstId, secondId) },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PokemonCompareEvent.NavigateBack -> onBack()
            }
        }
    }

    val first = state.first
    val second = state.second

    // Destaque do maior por stat só quando ambas as colunas carregaram (Q2=B).
    val winnerByLabel: Map<String, br.com.pokedex.feature.pokemoncompare.ui.model.StatWinner>? = remember(first, second) {
        if (first is SideUiState.Success && second is SideUiState.Success) {
            buildStatComparison(first.pokemon, second.pokemon)
                .associate { row: StatComparisonRow -> row.label to row.winner }
        } else {
            null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(White)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(PokedexRed)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = { viewModel.onIntent(PokemonCompareIntent.NavigateBack) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = White,
                )
            }
            Text(text = "Compare", style = HeadlineBold, color = White)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
        ) {
            CompareColumn(
                side = CompareSide.FIRST,
                state = first,
                winnerByLabel = winnerByLabel,
                onRetry = { viewModel.onIntent(PokemonCompareIntent.Retry(CompareSide.FIRST)) },
                modifier = Modifier.weight(1f).testTag("compare-column-first"),
            )
            VerticalDivider(color = androidx.compose.ui.graphics.Color(0xFFE0E0E0))
            CompareColumn(
                side = CompareSide.SECOND,
                state = second,
                winnerByLabel = winnerByLabel,
                onRetry = { viewModel.onIntent(PokemonCompareIntent.Retry(CompareSide.SECOND)) },
                modifier = Modifier.weight(1f).testTag("compare-column-second"),
            )
        }
    }
}
