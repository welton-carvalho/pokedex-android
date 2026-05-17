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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.pokedex.core.designsystem.component.ErrorContent
import br.com.pokedex.core.designsystem.component.LoadingIndicator
import br.com.pokedex.core.designsystem.component.PokemonTypeChip
import br.com.pokedex.core.designsystem.theme.Body2Regular
import br.com.pokedex.core.designsystem.theme.Gray2
import br.com.pokedex.core.designsystem.theme.HeadlineBold
import br.com.pokedex.core.designsystem.theme.Subtitle1Bold
import br.com.pokedex.core.designsystem.theme.Subtitle2Bold
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
    onNavigateToNext: (Int) -> Unit = {},
    viewModel: PokemonDetailViewModel = koinViewModel(
        key = "pokemon_detail_$pokemonId",
        parameters = { parametersOf(pokemonId) },
    ),
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
                onNavigateToNext = { onNavigateToNext(state.pokemon!!.id + 1) },
            )
        }
    }
}

@Composable
private fun PokemonDetailContent(
    pokemon: PokemonDetailUiModel,
    onBack: () -> Unit,
    onNavigateToNext: () -> Unit,
) {
    val typeColor = pokemon.typeColor

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(typeColor),
    ) {
        // Pokeball watermark — top-right, partially off screen (matches Figma: center x=248, y=112)
        PokeballWatermark(
            color = White,
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(y = 18.dp),
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
                    color = White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(end = 16.dp),
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // White content card — starts at 160dp (Figma: white card y=224, header y=64)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 160.dp)
                        .background(
                            color = White,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        )
                        .padding(top = 56.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Type chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        pokemon.types.forEach { type ->
                            PokemonTypeChip(typeName = type)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "About", style = Subtitle1Bold, color = typeColor)

                    Spacer(modifier = Modifier.height(16.dp))

                    AboutSection(pokemon = pokemon, typeColor = typeColor)

                    if (pokemon.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = pokemon.description,
                            style = Body2Regular,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(text = "Base Stats", style = Subtitle1Bold, color = typeColor)

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        pokemon.stats.forEach { stat ->
                            PokemonStatBar(
                                label = stat.label,
                                value = stat.value,
                                color = typeColor,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Pokemon artwork — top at 16dp from inner box (Figma: image y=80, header=64)
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 16.dp),
                )

                // Navigation chevrons — centered at 132dp (Figma: chevron y=196, header=64)
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = 108.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Pokémon",
                        tint = White,
                    )
                }
                IconButton(
                    onClick = onNavigateToNext,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = 108.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Pokémon",
                        tint = White,
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutSection(
    pokemon: PokemonDetailUiModel,
    typeColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AboutItem(
            icon = Icons.Default.FitnessCenter,
            value = pokemon.weight,
            label = "Weight",
        )

        VerticalDivider(
            modifier = Modifier.height(48.dp),
            thickness = 1.dp,
            color = Color(0xFFE0E0E0),
        )

        AboutItem(
            icon = Icons.Default.Straighten,
            value = pokemon.height,
            label = "Height",
        )

        VerticalDivider(
            modifier = Modifier.height(48.dp),
            thickness = 1.dp,
            color = Color(0xFFE0E0E0),
        )

        // Moves — sem ícone, só lista de abilities
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            pokemon.abilities.forEach { ability ->
                Text(
                    text = ability,
                    style = Subtitle2Bold,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Moves",
                style = Body2Regular,
                color = Gray2,
            )
        }
    }
}

@Composable
private fun AboutItem(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF212121),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = value,
                style = Subtitle2Bold,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = Body2Regular,
            color = Gray2,
        )
    }
}

@Composable
private fun PokeballWatermark(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.drawBehind {
            val radius = size.minDimension / 2f
            val stroke = radius * 0.06f
            val innerRadius = radius * 0.22f
            val c = color.copy(alpha = 0.25f)

            drawCircle(color = c, radius = radius, style = Stroke(width = stroke))
            drawLine(
                color = c,
                start = Offset(0f, center.y),
                end = Offset(size.width, center.y),
                strokeWidth = stroke,
            )
            drawCircle(color = c, radius = innerRadius + stroke, style = Stroke(width = stroke))
            drawCircle(color = c, radius = innerRadius)
        },
    )
}
