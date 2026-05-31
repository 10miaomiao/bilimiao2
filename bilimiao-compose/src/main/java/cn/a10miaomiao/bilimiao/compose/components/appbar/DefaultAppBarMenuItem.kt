package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign

@Composable
fun DefaultAppBarMenuItem(
    data: MenuItemData,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppBarMenuItemLayout(
        data = data,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun AppBarMenuItemLayout(
    data: MenuItemData,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = appBarMenuItemColors(checked = false)
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .widthIn(min = AppBarConfig.MenuItemMinWidth)
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = { onClick(data) }
            )
            .padding(
                start = AppBarConfig.NavigationPadding,
                end = AppBarConfig.NavigationPadding,
            )
            .appBarMenuItemSemantics(data),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppBarMenuItemIcon(data = data, contentColor = colors.contentColor)
        AppBarMenuItemTexts(
            data = data,
            contentColor = colors.contentColor,
            subContentColor = colors.subContentColor,
            textAlign = TextAlign.Center,
        )
    }
}
