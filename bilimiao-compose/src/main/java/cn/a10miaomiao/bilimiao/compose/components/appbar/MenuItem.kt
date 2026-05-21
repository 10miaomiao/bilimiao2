package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.R

internal data class AppBarMenuItemColors(
    val contentColor: Color,
    val subContentColor: Color,
)

@Composable
internal fun appBarMenuItemColors(
    checked: Boolean,
): AppBarMenuItemColors {
    val contentColor = if (checked) {
        MaterialTheme.colorScheme.primary
    } else {
        LocalContentColor.current
    }
    val subContentColor = if (checked) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    } else {
        contentColor.copy(alpha = 0.7f)
    }
    return AppBarMenuItemColors(
        contentColor = contentColor,
        subContentColor = subContentColor,
    )
}

internal fun Modifier.appBarMenuItemSemantics(data: MenuItemData): Modifier {
    return semantics {
        data.contentDescription?.let { contentDescription = it }
    }
}

@Composable
internal fun AppBarMenuItemIcon(
    modifier: Modifier = Modifier,
    data: MenuItemData,
    contentColor: Color,
) {
    if (data.iconVector != null) {
        Icon(
            imageVector = data.iconVector,
            contentDescription = data.title,
            tint = contentColor,
            modifier = modifier
                .size(AppBarConfig.MenuItemIconSize),
        )
    } else if (data.iconResource != null) {
        Icon(
            painter = painterResource(id = data.iconResource),
            contentDescription = data.title,
            tint = contentColor,
            modifier = modifier
                .size(AppBarConfig.MenuItemIconSize),
        )
    }
}

@Composable
internal fun AppBarMenuItemTexts(
    data: MenuItemData,
    contentColor: Color,
    subContentColor: Color,
    textAlign: TextAlign,
    replaceSubTitle: Boolean = true,
    titleFontSize: androidx.compose.ui.unit.TextUnit = AppBarConfig.TitleTextSize.value.sp,
    subTitleFontSize: androidx.compose.ui.unit.TextUnit = AppBarConfig.SubTitleTextSize.value.sp,
) {
    if (data.title.isNotEmpty()) {
        Text(
            text = data.title,
            color = contentColor,
            fontSize = titleFontSize,
            lineHeight = titleFontSize,
            textAlign = textAlign,
        )
    }

    data.subTitle?.let { subTitle ->
        Text(
            text = if (replaceSubTitle) {
                subTitle.replace("\n", " ")
            } else {
                subTitle
            },
            color = subContentColor,
            fontSize = subTitleFontSize,
            lineHeight = subTitleFontSize * 1.5,
            textAlign = textAlign,
            modifier = Modifier
                .padding(top = AppBarConfig.SubTitleMarginTop),
        )
    }
}

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
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
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
