package br.com.pokedex.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.pokedex.core.designsystem.theme.Body3Regular
import br.com.pokedex.core.designsystem.theme.Gray1
import br.com.pokedex.core.designsystem.theme.Gray3
import br.com.pokedex.core.designsystem.theme.Subtitle3Bold

private const val MAX_STAT = 255

@Composable
fun PokemonStatBar(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val fraction = remember(value) { (value / MAX_STAT.toFloat()).coerceIn(0f, 1f) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = Subtitle3Bold,
            color = color,
            modifier = Modifier.width(36.dp),
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(12.dp)
                .background(color.copy(alpha = 0.3f)),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value.toString().padStart(3, '0'),
            style = Body3Regular,
            color = Gray1,
            modifier = Modifier.width(28.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Gray3)
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}
