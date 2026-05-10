package br.com.pokedex.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.pokedex.core.designsystem.theme.Body2Regular
import br.com.pokedex.core.designsystem.theme.Gray2

@Composable
fun EmptyContent(
    message: String = "No Pokémon found.",
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Text(text = message, style = Body2Regular, color = Gray2)
    }
}
