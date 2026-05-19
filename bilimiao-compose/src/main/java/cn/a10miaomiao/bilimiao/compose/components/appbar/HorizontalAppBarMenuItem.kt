package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.appbar.AppBarConfig.NavigationPadding

@Composable
fun HorizontalAppBarMenuItem(
    data: MenuItemData,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalAppBarMenuItemLayout(
        data = data,
        checked = false,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun HorizontalAppBarMenuItemLayout(
    data: MenuItemData,
    checked: Boolean,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = appBarMenuItemColors(checked = checked)
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .width(AppBarConfig.MenuWidth)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = { onClick(data) }
            )
            .padding(NavigationPadding)
            .appBarMenuItemSemantics(data),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppBarMenuItemIcon(data = data, contentColor = colors.contentColor)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            AppBarMenuItemTexts(
                data = data,
                contentColor = colors.contentColor,
                subContentColor = colors.subContentColor,
                textAlign = TextAlign.Start,
            )
        }
    }
}
