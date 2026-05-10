package br.com.pokedex.feature.pokemonlist.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.theme.CaptionRegular
import br.com.pokedex.core.designsystem.theme.Gray1
import br.com.pokedex.core.designsystem.theme.Gray2
import br.com.pokedex.core.designsystem.theme.GrayBackground
import br.com.pokedex.core.designsystem.theme.Subtitle3Bold
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.feature.pokemonlist.ui.model.PokemonListUiModel
import coil3.compose.AsyncImage

@Composable
fun PokemonCard(
    pokemon: PokemonListUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .height(108.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    .padding(vertical = 6.dp, horizontal = 4.dp),
            ) {
                Text(
                    text = pokemon.displayName,
                    style = Subtitle3Bold,
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
                    .padding(top = 6.dp, end = 6.dp),
            )

            // Pokemon artwork — centered in the upper area
            AsyncImage(
                model = pokemon.imageUrl,
                contentDescription = pokemon.displayName,
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.Center)
                    .offset(y = (-8).dp),
            )
        }
    }
}
