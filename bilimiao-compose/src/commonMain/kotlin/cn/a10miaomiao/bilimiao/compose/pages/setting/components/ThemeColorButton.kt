package cn.a10miaomiao.bilimiao.compose.pages.setting.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.toHct

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ThemeColorButton(
    onClick: () -> Unit,
    baseColor: Color,
    colorName: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    cardColor: Color = MaterialTheme.colorScheme.surface,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    val containerSize by animateDpAsState(targetValue = if (selected) 28.dp else 0.dp)
    val iconSize by animateDpAsState(targetValue = if (selected) 16.dp else 0.dp)

    Surface(
        modifier = modifier
            .size(
                width = 100.dp,
                height = 100.dp,
            )
            .aspectRatio(1f),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val hct = baseColor.toHct()
            val color1 = Color(Hct.from(hct.hue, 40.0, 80.0).toInt())
            val color2 = Color(Hct.from(hct.hue, 40.0, 90.0).toInt())
            val color3 = Color(Hct.from(hct.hue, 40.0, 60.0).toInt())

            Box(
                modifier = modifier
                    .padding(top = 8.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .drawBehind { drawCircle(color1) },
            ) {
                Surface(
                    color = color2,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(24.dp),
                ) {}
                Surface(
                    color = color3,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp),
                ) {}
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .size(containerSize)
                        .drawBehind { drawCircle(containerColor) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize).align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Text(
                text = colorName,
                modifier = Modifier.padding(top = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) baseColor else MaterialTheme.colorScheme.outline,
            )
        }
    }
}