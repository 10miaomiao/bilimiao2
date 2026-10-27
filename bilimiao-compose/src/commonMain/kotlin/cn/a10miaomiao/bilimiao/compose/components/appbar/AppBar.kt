package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune

/**
 * AppBar 组件
 * 支持竖屏和横屏两种布局
 *
 * 竖屏布局为「标题条 + 菜单行」的底栏，页面滚动时按 [AppBarState.collapseProgress] 整体下滑，
 * 收起后只保留标题条（参照老项目 bbmiao 的 quick-return 行为）。
 *
 * @param title 标题
 * @param canBack 是否显示返回按钮
 * @param showPointer 是否显示指针图标
 * @param pointerOrientation 指针方向
 * @param showExchange 是否显示交换图标（横屏）
 * @param menus 菜单列表
 * @param isNavigationMenu 是否为导航菜单模式
 * @param checkedKey 当前选中的菜单项key
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
    appBarState: AppBarState,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onMenuItemClick: (MenuItemData) -> Unit = {},
    onPointerClick: () -> Unit = {},
    onExchangeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val safePadding = WindowInsets.safeDrawing.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
    ).asPaddingValues()
    val collapseRangePx = with(LocalDensity.current) { AppBarConfig.MenuHeight.toPx() }
    val interactionSource = remember { MutableInteractionSource() }
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.96f)

    // 整条 Surface 下滑一个菜单行高度：菜单行滑出可视区，标题条停在底部安全区上方，
    // 上方腾出的空间露出下面的内容；滑出窗口底部的部分靠 clipToBounds 裁掉。
    // 收缩进度在绘制阶段读取，滚动过程中不触发重组
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds(),
    ) {
        Surface(
            color = surfaceColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = collapseRangePx * appBarState.collapseProgress
                },
        ) {
            Column(
                modifier = Modifier.padding(safePadding)
            ) {
                // 标题区域（收起后标题条变成唯一的可点击入口，点击重新展开菜单行）
                AppBarTitle(
                    title = title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppBarConfig.TitleHeight)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) {
                            // 展开态下是空操作，收起态下动画展开
                            appBarState.expandAppBar()
                        }
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
                    appBarState = appBarState,
                    onBackClick = onBackClick,
                    onMenuClick = onMenuClick,
                    onMenuItemClick = onMenuItemClick,
                    onPointerClick = onPointerClick,
                    onExchangeClick = onExchangeClick,
                )
            }
        }
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
 *
 * 透明度随收缩进度在绘制阶段淡出，滚动时不参与重组
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
            .padding(start = AppBarConfig.NavigationPadding)
            .graphicsLayer { alpha = 1f - appBarState.collapseProgress },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationIcon(
            icon = if (canBack) AppBarNavigationIcon.Back else AppBarNavigationIcon.Menu,
            onClick = if (canBack) onBackClick else onMenuClick,
        )

        if (showPointer) {
            AppBarIconButton(
                icon = Icons.Default.Tune,
                contentDescription = "指针",
                rotation = if (pointerOrientation) 0f else 180f,
                onClick = onPointerClick,
            )
        }

        if (showExchange) {
            AppBarIconButton(
                icon = Icons.Default.SwapVert,
                contentDescription = "交换",
                onClick = onExchangeClick,
            )
        }

        if (isNavigationMenu) {
            LazyRow(
                modifier = Modifier
                    .weight(1f),
                horizontalArrangement = Arrangement.End,
            ) {
                items(
                    items = menus,
                    key = { it.key },
                ) { menuItem ->
                    AppBarNavigationItem(
                        data = menuItem,
                        checked = checkedKey == menuItem.key,
                        onClick = onMenuItemClick,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        } else {
            VerticalAppBarMenus(
                menus = menus,
                isNavigationMenu = isNavigationMenu,
                checkedKey = checkedKey,
                appBarState = appBarState,
                onMenuItemClick = onMenuItemClick,
            )
        }
    }
}

@Composable
private fun AppBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    rotation: Float = 0f,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = LocalContentColor.current.copy(alpha = 0.45f),
        modifier = Modifier
            .rotate(rotation)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = onClick,
            ),
    )
}
