package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AppBarNavigationItem(
    data: MenuItemData,
    checked: Boolean,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant
    val colors = AppBarMenuItemColors(
        contentColor = if (checked) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        subContentColor = if (checked) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
    val containerAlpha by animateFloatAsState(
        targetValue = if (checked) 1f else .75f,
        animationSpec = spring(),
        label = "appBarNavigationItemContainerAlpha",
    )
    val containerScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(),
        label = "appBarNavigationItemContainerScale",
    )

    Box(
        modifier = modifier
            .widthIn(min = AppBarConfig.MenuItemMinWidth)
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = { onClick(data) })
            .appBarMenuItemSemantics(data),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(48.dp)
                .scale(containerScale)
                .graphicsLayer { alpha = containerAlpha }
                .clip(RoundedCornerShape(8.dp))
                .background(selectedContainerColor),
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AppBarMenuItemIcon(
                modifier = Modifier
                    .padding(bottom = AppBarConfig.MenuItemIconMargin),
                data = data,
                contentColor = colors.contentColor,
            )
            AppBarMenuItemTexts(
                data = data,
                contentColor = colors.contentColor,
                subContentColor = colors.subContentColor,
                textAlign = TextAlign.Center,
            )
        }
    }

}
