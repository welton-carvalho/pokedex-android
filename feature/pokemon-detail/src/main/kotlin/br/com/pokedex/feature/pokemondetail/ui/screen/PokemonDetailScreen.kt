package br.com.pokedex.feature.pokemondetail.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.pokedex.core.designsystem.component.ErrorContent
import br.com.pokedex.core.designsystem.component.LoadingIndicator
import br.com.pokedex.core.designsystem.component.PokemonTypeChip
import br.com.pokedex.core.designsystem.theme.Body2Regular
import br.com.pokedex.core.designsystem.theme.Body3Regular
import br.com.pokedex.core.designsystem.theme.Gray2
import br.com.pokedex.core.designsystem.theme.Gray3
import br.com.pokedex.core.designsystem.theme.HeadlineBold
import br.com.pokedex.core.designsystem.theme.Subtitle1Bold
import br.com.pokedex.core.designsystem.theme.Subtitle2Bold
import br.com.pokedex.core.designsystem.theme.Subtitle3Bold
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.core.ui.PokemonStatBar
import br.com.pokedex.feature.pokemondetail.ui.event.PokemonDetailEvent
import br.com.pokedex.feature.pokemondetail.ui.intent.PokemonDetailIntent
import br.com.pokedex.feature.pokemondetail.ui.model.PokemonDetailUiModel
import br.com.pokedex.feature.pokemondetail.viewmodel.PokemonDetailViewModel
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PokemonDetailScreen(
    pokemonId: Int,
    onBack: () -> Unit,
    viewModel: PokemonDetailViewModel = koinViewModel(parameters = { parametersOf(pokemonId) }),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PokemonDetailEvent.NavigateBack -> onBack()
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
                onRetry = { viewModel.onIntent(PokemonDetailIntent.Retry) },
            )
        }
        state.pokemon != null -> {
            PokemonDetailContent(
                pokemon = state.pokemon!!,
                onBack = { viewModel.onIntent(PokemonDetailIntent.NavigateBack) },
            )
        }
    }
}

@Composable
private fun PokemonDetailContent(
    pokemon: PokemonDetailUiModel,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(pokemon.typeColor),
    ) {
        // Pokéball watermark
        Text(
            text = "⬤",
            style = HeadlineBold.copy(fontSize = androidx.compose.ui.unit.TextUnit(200f, androidx.compose.ui.unit.TextUnitType.Sp)),
            color = White.copy(alpha = 0.1f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-30).dp),
        )

        Column(modifier = Modifier.fillMaxSize()) {
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
                    text = pokemon.name,
                    style = HeadlineBold,
                    color = White,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    style = Subtitle2Bold,
                    color = White,
                    modifier = Modifier.padding(end = 16.dp),
                )
            }

            // Artwork
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.name,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .offset(y = 56.dp),
            )

            // White card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = White,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    )
                    .padding(top = 56.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    // Type chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pokemon.types.forEach { type ->
                            PokemonTypeChip(typeName = type)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // About section
                    Text(text = "About", style = Subtitle1Bold, color = pokemon.typeColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = pokemon.weight, style = Subtitle2Bold)
                            Text(text = "Weight", style = Body3Regular, color = Gray2)
                        }
                        Divider(
                            color = Gray3,
                            modifier = Modifier
                                .height(32.dp)
                                .size(width = 1.dp, height = 32.dp),
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = pokemon.height, style = Subtitle2Bold)
                            Text(text = "Height", style = Body3Regular, color = Gray2)
                        }
                        Divider(
                            color = Gray3,
                            modifier = Modifier
                                .height(32.dp)
                                .size(width = 1.dp, height = 32.dp),
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            pokemon.abilities.forEach { ability ->
                                Text(text = ability, style = Body3Regular, textAlign = TextAlign.Center)
                            }
                            Text(text = "Moves", style = Body3Regular, color = Gray2)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Base stats
                    Text(text = "Base Stats", style = Subtitle1Bold, color = pokemon.typeColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    pokemon.stats.forEach { stat ->
                        PokemonStatBar(
                            label = stat.label,
                            value = stat.value,
                            color = pokemon.typeColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
