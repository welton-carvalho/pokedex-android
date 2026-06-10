package br.com.pokedex.feature.pokemonlist.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.theme.Body3Regular
import br.com.pokedex.core.designsystem.theme.CaptionRegular
import br.com.pokedex.core.designsystem.theme.Gray1
import br.com.pokedex.core.designsystem.theme.Gray2
import br.com.pokedex.core.designsystem.theme.GrayBackground
import br.com.pokedex.core.designsystem.theme.PokedexRed
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.feature.pokemonlist.ui.model.PokemonListUiModel
import coil3.compose.AsyncImage

@Composable
fun PokemonCard(
    pokemon: PokemonListUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val shape = RoundedCornerShape(8.dp)
    Card(
        modifier = modifier
            .height(108.dp)
            .clickable(onClick = onClick)
            .then(if (selected) Modifier.border(2.dp, PokedexRed, shape) else Modifier)
            .testTag("pokemon-card-${pokemon.id}"),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (selected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(PokedexRed),
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            // Name tag anchored to bottom
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = GrayBackground,
                        shape = RoundedCornerShape(7.dp),
                    )
                    .padding(top = 24.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
            ) {
                Text(
                    text = pokemon.displayName,
                    style = Body3Regular,
                    color = Gray1,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Pokemon number — top right
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = CaptionRegular,
                color = Gray2,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 8.dp),
            )

            // Pokemon artwork — top left
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.displayName,
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 16.dp, y = 16.dp),
            )
        }
    }
}
