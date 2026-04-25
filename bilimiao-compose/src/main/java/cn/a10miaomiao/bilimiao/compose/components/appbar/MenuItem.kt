package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.R

/**
 * AppBar 菜单项
 *
 * @param data 菜单项数据
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun MenuItem(
    data: MenuItemData,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
    expandable: Boolean = false,
    expanded: Boolean = false,
    verticalLayout: Boolean = true,
    depth: Int = 0,
) {
    BaseMenuItem(
        data = data,
        checked = false,
        themeColor = Color.Unspecified,
        onClick = onClick,
        modifier = modifier,
        expandable = expandable,
        expanded = expanded,
        verticalLayout = verticalLayout,
        depth = depth,
    )
}

@Composable
internal fun BaseMenuItem(
    data: MenuItemData,
    checked: Boolean,
    themeColor: Color,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
    expandable: Boolean = false,
    expanded: Boolean = false,
    verticalLayout: Boolean = true,
    depth: Int = 0,
) {
    val contentColor = if (checked) {
        themeColor
    } else {
        LocalContentColor.current
    }
    val subContentColor = if (checked) {
        themeColor.copy(alpha = 0.7f)
    } else {
        contentColor.copy(alpha = 0.45f)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val paddingStart = if (verticalLayout) {
        AppBarConfig.NavigationIconPadding + (depth * 12).dp
    } else {
        0.dp
    }

    val containerModifier = modifier
        .width(if (verticalLayout) AppBarConfig.MenuWidth else AppBarConfig.MenuItemMinWidth)
        .clickable(
            interactionSource = interactionSource,
            indication = ripple(bounded = false),
            onClick = { onClick(data) }
        )
        .padding(
            start = paddingStart,
            end = AppBarConfig.NavigationIconPadding,
            top = AppBarConfig.NavigationIconPadding,
            bottom = AppBarConfig.NavigationIconPadding,
        )
        .semantics {
            data.contentDescription?.let { contentDescription = it }
        }

    if (verticalLayout) {
        Row(
            modifier = containerModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MenuItemIcon(data = data, contentColor = contentColor)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                MenuItemTexts(
                    data = data,
                    contentColor = contentColor,
                    subContentColor = subContentColor,
                    textAlign = TextAlign.Start,
                )
            }
            if (expandable) {
                ExpandIndicator(expanded = expanded, contentColor = subContentColor)
            }
        }
    } else {
        Column(
            modifier = containerModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                MenuItemIcon(data = data, contentColor = contentColor)
            }
            MenuItemTexts(
                data = data,
                contentColor = contentColor,
                subContentColor = subContentColor,
                textAlign = TextAlign.Center,
            )
            if (expandable) {
                ExpandIndicator(expanded = expanded, contentColor = subContentColor)
            }
        }
    }
}

@Composable
private fun MenuItemIcon(
    data: MenuItemData,
    contentColor: Color,
) {
    Box(
        modifier = Modifier.size(AppBarConfig.MenuItemIconSize),
        contentAlignment = Alignment.Center,
    ) {
        when {
            data.iconVector != null -> {
                Icon(
                    imageVector = data.iconVector,
                    contentDescription = data.title,
                    tint = contentColor,
                    modifier = Modifier.size(AppBarConfig.MenuItemIconSize),
                )
            }
            data.iconResource != null -> {
                Icon(
                    painter = painterResource(id = data.iconResource),
                    contentDescription = data.title,
                    tint = contentColor,
                    modifier = Modifier.size(AppBarConfig.MenuItemIconSize),
                )
            }
        }
    }
}

@Composable
private fun MenuItemTexts(
    data: MenuItemData,
    contentColor: Color,
    subContentColor: Color,
    textAlign: TextAlign,
) {
    if (data.title.isNotEmpty()) {
        Text(
            text = data.title,
            color = contentColor,
            fontSize = AppBarConfig.TitleTextSize.value.sp,
            textAlign = textAlign,
            modifier = Modifier.padding(top = AppBarConfig.SubTitleMarginTop),
        )
    }

    data.subTitle?.let { subTitle ->
        Text(
            text = subTitle.replace("\n", " "),
            color = subContentColor,
            fontSize = AppBarConfig.SubTitleTextSize.value.sp,
            textAlign = textAlign,
            modifier = Modifier.padding(top = AppBarConfig.SubTitleMarginTop),
        )
    }
}

@Composable
private fun ExpandIndicator(
    expanded: Boolean,
    contentColor: Color,
) {
    Text(
        text = if (expanded) "▴" else "▾",
        color = contentColor,
        fontSize = AppBarConfig.TitleTextSize.value.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(AppBarConfig.NavigationIconSize),
    )
}

/**
 * AppBar 导航按钮
 *
 * @param icon 类型
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param customIconResource 自定义图标资源（当 icon == Custom 时使用）
 */
@Composable
fun NavigationIcon(
    icon: AppBarNavigationIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    customIconResource: Int? = null,
) {
    val contentColor = LocalContentColor.current
    val interactionSource = remember { MutableInteractionSource() }

    val iconResource = when (icon) {
        AppBarNavigationIcon.Back -> R.drawable.ic_back_24dp
        AppBarNavigationIcon.Menu -> R.drawable.ic_baseline_menu_24
        AppBarNavigationIcon.Custom -> customIconResource ?: R.drawable.ic_back_24dp
    }

    Box(
        modifier = modifier
            .size(AppBarConfig.MenuItemMinWidth, AppBarConfig.MenuHeight)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = onClick
            )
            .padding(AppBarConfig.NavigationIconPadding),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = iconResource),
            contentDescription = when (icon) {
                AppBarNavigationIcon.Back -> "返回"
                AppBarNavigationIcon.Menu -> "菜单"
                AppBarNavigationIcon.Custom -> null
            },
            tint = contentColor,
            modifier = Modifier.size(AppBarConfig.NavigationIconSize),
        )
    }
}
