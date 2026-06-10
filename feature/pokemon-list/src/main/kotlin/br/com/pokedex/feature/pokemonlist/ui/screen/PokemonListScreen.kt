package br.com.pokedex.feature.pokemonlist.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import br.com.pokedex.core.designsystem.component.ErrorContent
import br.com.pokedex.core.designsystem.theme.HeadlineBold
import br.com.pokedex.core.designsystem.theme.PokedexRed
import br.com.pokedex.core.designsystem.theme.Subtitle2Bold
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.feature.pokemonlist.ui.component.PokemonCard
import br.com.pokedex.feature.pokemonlist.ui.component.PokemonLoadingCard
import br.com.pokedex.feature.pokemonlist.ui.event.PokemonListEvent
import br.com.pokedex.feature.pokemonlist.ui.intent.PokemonListIntent
import br.com.pokedex.feature.pokemonlist.viewmodel.PokemonListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PokemonListScreen(
    onNavigateToDetail: (Int) -> Unit,
    onNavigateToCompare: (Int, Int) -> Unit,
    viewModel: PokemonListViewModel = koinViewModel(),
) {
    val pagingItems = viewModel.pokemonPagingFlow.collectAsLazyPagingItems()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PokemonListEvent.NavigateToDetail -> onNavigateToDetail(event.id)
                is PokemonListEvent.NavigateToCompare -> onNavigateToCompare(event.firstId, event.secondId)
                is PokemonListEvent.ShowSelectionLimit ->
                    snackbarHostState.showSnackbar("You can only compare 2 Pokémon")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PokedexRed),
        ) {
            PokemonListHeader(
                isSelectionMode = state.isSelectionMode,
                onToggleSelectionMode = { viewModel.onIntent(PokemonListIntent.ToggleSelectionMode) },
                modifier = Modifier.statusBarsPadding(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = White,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                    ),
            ) {
                when {
                    pagingItems.loadState.refresh is LoadState.Error -> {
                        ErrorContent(
                            message = "Something went wrong. Please try again.",
                            onRetry = {
                                viewModel.onIntent(PokemonListIntent.Retry)
                                pagingItems.retry()
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (pagingItems.loadState.refresh is LoadState.Loading) {
                                items(18) {
                                    PokemonLoadingCard()
                                }
                            } else {
                                items(
                                    count = pagingItems.itemCount,
                                    key = { index -> pagingItems.peek(index)?.id ?: index },
                                    contentType = { "pokemon" },
                                ) { index ->
                                    val pokemon = pagingItems[index]
                                    if (pokemon != null) {
                                        PokemonCard(
                                            pokemon = pokemon,
                                            selected = pokemon.id in state.selectedIds,
                                            onClick = {
                                                if (state.isSelectionMode) {
                                                    viewModel.onIntent(PokemonListIntent.ToggleSelection(pokemon.id))
                                                } else {
                                                    viewModel.onIntent(PokemonListIntent.ClickPokemon(pokemon.id))
                                                }
                                            },
                                        )
                                    } else {
                                        PokemonLoadingCard()
                                    }
                                }
                                if (pagingItems.loadState.append is LoadState.Loading) {
                                    items(3) { PokemonLoadingCard() }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.isSelectionMode) {
            Button(
                onClick = { viewModel.onIntent(PokemonListIntent.ClickCompare) },
                enabled = state.canCompare,
                colors = ButtonDefaults.buttonColors(containerColor = PokedexRed),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .testTag("compare-action-button"),
            ) {
                Text(text = "Compare (${state.selectedIds.size})", color = White)
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }
}

@Composable
private fun PokemonListHeader(
    isSelectionMode: Boolean,
    onToggleSelectionMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Pokédex",
                style = HeadlineBold,
                color = White,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (isSelectionMode) "Cancel" else "Compare",
                style = Subtitle2Bold,
                color = White,
                modifier = Modifier
                    .clickable(onClick = onToggleSelectionMode)
                    .padding(8.dp)
                    .testTag("compare-mode-toggle"),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = PokedexRed,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Search", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}
