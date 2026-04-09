package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMenuItemClick: (MenuItemData) -> Unit,
    onPointerClick: () -> Unit,
    onExchangeClick: () -> Unit,
) {
    val contentColor = LocalContentColor.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
//            .height(AppBarConfig.MenuHeight)
            .padding(horizontal = AppBarConfig.NavigationIconPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 导航图标
        NavigationIcon(
            icon = if (canBack) AppBarNavigationIcon.Back else AppBarNavigationIcon.Menu,
            onClick = if (canBack) onBackClick else onMenuClick,
        )

        // 指针图标（可选）
        if (showPointer) {
            val pointerRotation = if (pointerOrientation) 0f else 180f
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, _ -> },
                            onDragEnd = { onPointerClick() }
                        )
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pointer_24dp),
                    contentDescription = "指针",
                    tint = contentColor.copy(alpha = 0.45f),
                    modifier = Modifier
                        .padding(AppBarConfig.NavigationIconPadding)
                        .then(
                            if (pointerRotation != 0f) {
                                Modifier.layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)
                                    layout(constraints.maxWidth, constraints.maxHeight) {
                                        placeable.placeRelative(
                                            constraints.maxWidth - placeable.width,
                                            (constraints.maxHeight - placeable.height) / 2
                                        )
                                    }
                                }
                            } else Modifier
                        ),
                )
            }
        }

        // 交换图标（可选）
        if (showExchange) {
            Icon(
                painter = painterResource(id = R.drawable.ic_exchange_24dp),
                contentDescription = "交换",
                tint = contentColor.copy(alpha = 0.45f),
                modifier = Modifier
                    .padding(AppBarConfig.NavigationIconPadding)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, _ -> },
                            onDragEnd = { onExchangeClick() }
                        )
                    },
            )
        }

        // 菜单列表
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.End,
        ) {
            menus.forEach { menuItem ->
                if (isNavigationMenu) {
                    CheckableMenuItem(
                        data = menuItem,
                        checked = checkedKey == menuItem.key,
                        themeColor = themeColor,
                        onClick = onMenuItemClick,
                    )
                } else {
                    MenuItem(
                        data = menuItem,
                        onClick = onMenuItemClick,
                    )
                }
            }
        }
    }
}
