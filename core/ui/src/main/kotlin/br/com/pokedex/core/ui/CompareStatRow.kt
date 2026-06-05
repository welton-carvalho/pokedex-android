package br.com.pokedex.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.theme.Gray1
import br.com.pokedex.core.designsystem.theme.Subtitle2Bold
import br.com.pokedex.core.designsystem.theme.TypeFire
import br.com.pokedex.core.designsystem.theme.TypeGrass

/**
 * Renders a single stat comparison row between two Pokémon, with a single central [label] and one
 * [PokemonStatBar] per side. Bar rendering is fully delegated to the existing [PokemonStatBar]
 * composable — no bar geometry is reimplemented here.
 */
@Composable
fun CompareStatRow(
    label: String,
    valueA: Int,
    valueB: Int,
    colorA: Color,
    colorB: Color,
    modifier: Modifier = Modifier,
    maxValue: Int = 255,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        PokemonStatBar(
            label = "",
            value = valueA,
            color = colorA,
            maxValue = maxValue,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = Subtitle2Bold,
            color = Gray1,
            modifier = Modifier
                .width(44.dp)
                .padding(horizontal = 4.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        PokemonStatBar(
            label = "",
            value = valueB,
            color = colorB,
            maxValue = maxValue,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CompareStatRowPreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CompareStatRow(
            label = "HP",
            valueA = 45,
            valueB = 78,
            colorA = TypeGrass,
            colorB = TypeFire,
        )
    }
}
