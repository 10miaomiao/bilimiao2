package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune

/**
 * AppBar 横屏布局
 * 导航图标在左侧，菜单在右侧垂直排列
 *
 * @param title 标题
 * @param showBack 是否显示返回按钮
 * @param showPointer 是否显示指针图标
 * @param pointerOrientation 指针方向
 * @param showExchange 是否显示交换图标
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
fun AppBarHorizontal(
    title: String?,
    showBack: Boolean = true,
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
    val contentColor = LocalContentColor.current
    val safePadding = WindowInsets.safeDrawing.only(
        WindowInsetsSides.Vertical + WindowInsetsSides.Left
    ).asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(AppBarConfig.MenuWidth)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(safePadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 返回/菜单按钮
        NavigationIcon(
            icon = if (showBack) AppBarNavigationIcon.Back else AppBarNavigationIcon.Menu,
            onClick = if (showBack) onBackClick else onMenuClick,
            modifier = Modifier.width(AppBarConfig.MenuWidth),
        )

        if (showPointer) {
            AppBarHorizontalIconButton(
                icon = Icons.Default.Tune,
                contentDescription = "指针",
                rotation = if (pointerOrientation) 0f else 180f,
                onClick = onPointerClick,
            )
        }

        if (showExchange) {
            AppBarHorizontalIconButton(
                icon = Icons.Default.SwapVert,
                contentDescription = "交换",
                onClick = onExchangeClick,
            )
        }

        // 标题
        if (!title.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(
                    bottom = AppBarConfig.NavigationPadding,
                ),
                text = title,
                color = contentColor,
                fontSize = AppBarConfig.TitleTextSize.value.sp,
                lineHeight = AppBarConfig.TitleTextSize.value.sp,
                textAlign = TextAlign.Center
            )
        }

        if (isNavigationMenu) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
            HorizontalAppBarMenus(
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
private fun AppBarHorizontalIconButton(
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
            .width(AppBarConfig.MenuWidth)
            .padding(AppBarConfig.NavigationPadding)
            .rotate(rotation)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = onClick,
            ),
    )
}
