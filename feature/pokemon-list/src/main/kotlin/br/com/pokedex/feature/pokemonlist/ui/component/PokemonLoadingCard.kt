package br.com.pokedex.feature.pokemonlist.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.component.ShimmerBox

@Composable
fun PokemonLoadingCard(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier
            .height(108.dp)
            .clip(RoundedCornerShape(8.dp))
            .fillMaxSize(),
    )
}
