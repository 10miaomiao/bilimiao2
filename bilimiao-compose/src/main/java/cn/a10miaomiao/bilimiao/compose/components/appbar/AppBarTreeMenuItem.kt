package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun VerticalTopLevelAppBarTreeMenuItem(
    data: MenuItemData,
    checked: Boolean,
    expandable: Boolean,
    expanded: Boolean,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
    checkable: Boolean = false,
) {
    val colors = appBarMenuItemColors(checked = checked)
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .widthIn(min = AppBarConfig.MenuItemMinWidth)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
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

@Composable
internal fun HorizontalTopLevelAppBarTreeMenuItem(
    data: MenuItemData,
    checked: Boolean,
    expandable: Boolean,
    expanded: Boolean,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
    checkable: Boolean = false,
) {
    val colors = appBarMenuItemColors(checked = checked)
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .clickable(
                onClick = { onClick(data) }
            )
            .padding(AppBarConfig.NavigationPadding)
            .appBarMenuItemSemantics(data),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        AppBarMenuItemIcon(
            data = data,
            contentColor = colors.contentColor,
        )
        Column {
            AppBarMenuItemTexts(
                data = data,
                contentColor = colors.contentColor,
                subContentColor = colors.subContentColor,
                textAlign = TextAlign.Center,
                replaceSubTitle = false,
            )
        }
    }
}

@Composable
internal fun AppBarTreeMenuItem(
    data: MenuItemData,
    expandable: Boolean,
    expanded: Boolean,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    TreeRowLayout(
        data = data,
        checked = false,
        expandable = expandable,
        expanded = expanded,
        onClick = onClick,
        modifier = modifier,
        checkable = false,
    )
}

@Composable
internal fun AppBarTreeCheckableMenuItem(
    data: MenuItemData,
    checked: Boolean,
    expandable: Boolean,
    expanded: Boolean,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    TreeRowLayout(
        data = data,
        checked = checked,
        expandable = expandable,
        expanded = expanded,
        onClick = onClick,
        modifier = modifier,
        checkable = true,
    )
}

@Composable
private fun TreeRowLayout(
    data: MenuItemData,
    checked: Boolean,
    expandable: Boolean,
    expanded: Boolean,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
    checkable: Boolean,
) {
    val colors = appBarMenuItemColors(checked = checked)
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = { onClick(data) }
            )
            .padding(AppBarConfig.NavigationPadding)
            .appBarMenuItemSemantics(data),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppBarMenuItemIcon(data = data, contentColor = colors.contentColor)
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                AppBarMenuItemTexts(
                    data = data,
                    contentColor = colors.contentColor,
                    subContentColor = colors.subContentColor,
                    textAlign = TextAlign.Start,
                    titleFontSize = 14.sp,
                    subTitleFontSize = 12.sp,
                )
            }
            if (checkable && checked) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "已选中",
                    tint = colors.contentColor,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (expandable) {
            TreeExpandIndicator(
                expanded = expanded,
                contentColor = if (checkable) colors.subContentColor else colors.contentColor.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun TreeExpandIndicator(
    expanded: Boolean,
    contentColor: Color,
) {
    androidx.compose.material3.Text(
        text = if (expanded) "▴" else "▾",
        color = contentColor,
        fontSize = AppBarConfig.TitleTextSize.value.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(AppBarConfig.NavigationIconSize),
    )
}
