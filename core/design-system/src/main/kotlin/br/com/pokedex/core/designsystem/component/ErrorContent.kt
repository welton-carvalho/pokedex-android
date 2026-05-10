package br.com.pokedex.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.theme.Body2Regular
import br.com.pokedex.core.designsystem.theme.Gray2
import br.com.pokedex.core.designsystem.theme.PokedexRed
import br.com.pokedex.core.designsystem.theme.Subtitle1Bold
import br.com.pokedex.core.designsystem.theme.Subtitle2Bold
import br.com.pokedex.core.designsystem.theme.White

@Composable
fun ErrorContent(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Text(text = "Oops!", style = Subtitle1Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, style = Body2Regular, color = Gray2)
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PokedexRed),
            ) {
                Text(text = "Retry", color = White, style = Subtitle2Bold)
            }
        }
    }
}
