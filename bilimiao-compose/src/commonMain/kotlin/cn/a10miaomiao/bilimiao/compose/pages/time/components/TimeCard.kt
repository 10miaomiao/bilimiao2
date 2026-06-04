package cn.a10miaomiao.bilimiao.compose.pages.time.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
internal fun TimeCard(
    title: String,
    active: Boolean = true,
    onActiveChange: (active: Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    val alpha by animateFloatAsState(targetValue = if(active) 1f else 0.6f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp, 0.dp, 10.dp, 10.dp)
            .alpha(alpha),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.clickable(
                enabled = !active,
                onClick = { onActiveChange(true) },
            ).padding(10.dp),
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(vertical = 5.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedVisibility(
                visible = active,
            ) {
                content()
            }
        }
    }
}