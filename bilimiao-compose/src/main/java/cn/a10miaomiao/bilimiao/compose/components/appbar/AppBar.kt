package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.R

/**
 * AppBar 组件
 * 支持竖屏和横屏两种布局
 *
 * @param title 标题
 * @param canBack 是否显示返回按钮
 * @param showPointer 是否显示指针图标
 * @param pointerOrientation 指针方向
 * @param showExchange 是否显示交换图标（横屏）
 * @param menus 菜单列表
 * @param isNavigationMenu 是否为导航菜单模式
 * @param checkedKey 当前选中的菜单项key
 * @param themeColor 主题色
 * @param backgroundColor 背景色
 * @param onBackClick 返回按钮点击
 * @param onMenuClick 菜单按钮点击
 * @param onMenuItemClick 菜单项点击
 * @param onPointerClick 指针点击
 * @param onExchangeClick 交换按钮点击
 * @param modifier 修饰符
 */
@Composable
fun AppBar(
    title: String?,
    canBack: Boolean = false,
    showPointer: Boolean = false,
    pointerOrientation: Boolean = true,
    showExchange: Boolean = false,
    menus: List<MenuItemData> = emptyList(),
    isNavigationMenu: Boolean = false,
    checkedKey: Int? = null,
    themeColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    menuExpanded: Boolean = true,
    appBarState: AppBarState,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onMenuItemClick: (MenuItemData) -> Unit = {},
    onPointerClick: () -> Unit = {},
    onExchangeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
    ) {
        // 标题区域
        AppBarTitle(
            title = title,
            modifier = Modifier
                .fillMaxWidth()
                .height(AppBarConfig.TitleHeight)
        )

        // 菜单区域
        AppBarMenuRow(
            canBack = canBack,
            showPointer = showPointer,
            pointerOrientation = pointerOrientation,
            showExchange = showExchange,
            menus = menus,
            isNavigationMenu = isNavigationMenu,
            checkedKey = checkedKey,
            themeColor = themeColor,
            menuExpanded = menuExpanded,
            appBarState = appBarState,
            onBackClick = onBackClick,
            onMenuClick = onMenuClick,
            onMenuItemClick = onMenuItemClick,
            onPointerClick = onPointerClick,
            onExchangeClick = onExchangeClick,
        )

        // 分割线
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = AppBarConfig.DividerHeight,
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

/**
 * AppBar 标题
 */
@Composable
private fun AppBarTitle(
    title: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (!title.isNullOrEmpty()) {
            Text(
                text = title.replace("\n", " "),
                color = LocalContentColor.current.copy(alpha = 0.45f),
                fontSize = AppBarConfig.TitleTextSize.value.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * AppBar 菜单行
 */
@Composable
private fun AppBarMenuRow(
    canBack: Boolean,
    showPointer: Boolean,
    pointerOrientation: Boolean,
    showExchange: Boolean,
    menus: List<MenuItemData>,
    isNavigationMenu: Boolean,
    checkedKey: Int?,
    themeColor: Color,
    menuExpanded: Boolean,
    appBarState: AppBarState,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMenuItemClick: (MenuItemData) -> Unit,
    onPointerClick: () -> Unit,
    onExchangeClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppBarConfig.MenuHeight)
            .padding(horizontal = AppBarConfig.NavigationIconPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationIcon(
            icon = if (canBack) AppBarNavigationIcon.Back else AppBarNavigationIcon.Menu,
            onClick = if (canBack) onBackClick else onMenuClick,
        )

        if (showPointer) {
            AppBarIconButton(
                iconRes = R.drawable.ic_pointer_24dp,
                contentDescription = "指针",
                rotation = if (pointerOrientation) 0f else 180f,
                onClick = onPointerClick,
            )
        }

        if (showExchange) {
            AppBarIconButton(
                iconRes = R.drawable.ic_exchange_24dp,
                contentDescription = "交换",
                onClick = onExchangeClick,
            )
        }

        AnimatedVisibility(
            visible = menuExpanded,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            VerticalAppBarMenus(
                menus = menus,
                isNavigationMenu = isNavigationMenu,
                checkedKey = checkedKey,
                themeColor = themeColor,
                appBarState = appBarState,
                onMenuItemClick = onMenuItemClick,
            )
        }
    }
}

@Composable
private fun AppBarIconButton(
    iconRes: Int,
    contentDescription: String,
    rotation: Float = 0f,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
        tint = LocalContentColor.current.copy(alpha = 0.45f),
        modifier = Modifier
            .padding(AppBarConfig.NavigationIconPadding)
            .rotate(rotation)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = onClick,
            ),
    )
}
