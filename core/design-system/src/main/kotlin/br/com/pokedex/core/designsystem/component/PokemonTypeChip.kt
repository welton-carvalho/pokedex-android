package br.com.pokedex.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.theme.Subtitle3Bold
import br.com.pokedex.core.designsystem.theme.TypeChipShape
import br.com.pokedex.core.designsystem.theme.White
import br.com.pokedex.core.designsystem.theme.pokemonTypeColor

@Composable
fun PokemonTypeChip(
    typeName: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = pokemonTypeColor(typeName),
) {
    Text(
        text = typeName.replaceFirstChar { it.uppercase() },
        style = Subtitle3Bold,
        color = White,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(TypeChipShape)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}
