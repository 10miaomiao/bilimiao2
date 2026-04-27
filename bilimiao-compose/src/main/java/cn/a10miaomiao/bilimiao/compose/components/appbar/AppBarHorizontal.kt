package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
fun AppBarHorizontal(
    title: String?,
    showBack: Boolean = true,
    showPointer: Boolean = false,
    pointerOrientation: Boolean = true,
    showExchange: Boolean = false,
    menus: List<MenuItemData> = emptyList(),
    isNavigationMenu: Boolean = false,
    checkedKey: Int? = null,
    themeColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    appBarState: AppBarState,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onMenuItemClick: (MenuItemData) -> Unit = {},
    onPointerClick: () -> Unit = {},
    onExchangeClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val contentColor = LocalContentColor.current

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(AppBarConfig.MenuWidth)
            .background(backgroundColor)
            .verticalScroll(rememberScrollState()),
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
                iconRes = R.drawable.ic_pointer_24dp,
                contentDescription = "指针",
                rotation = if (pointerOrientation) 0f else 180f,
                onClick = onPointerClick,
            )
        }

        if (showExchange) {
            AppBarHorizontalIconButton(
                iconRes = R.drawable.ic_exchange_24dp,
                contentDescription = "交换",
                onClick = onExchangeClick,
            )
        }

        // 标题
        if (!title.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .width(AppBarConfig.MenuWidth)
                    .padding(AppBarConfig.NavigationIconPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title.replace("\n", " "),
                    color = contentColor.copy(alpha = 0.45f),
                    fontSize = AppBarConfig.TitleTextSize.value.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // 分割线
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = AppBarConfig.DividerHeight,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        HorizontalAppBarMenus(
            menus = menus,
            isNavigationMenu = isNavigationMenu,
            checkedKey = checkedKey,
            themeColor = themeColor,
            appBarState = appBarState,
            onMenuItemClick = onMenuItemClick,
        )
    }
}

@Composable
private fun AppBarHorizontalIconButton(
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
            .width(AppBarConfig.MenuWidth)
            .padding(AppBarConfig.NavigationIconPadding)
            .rotate(rotation)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = onClick,
            ),
    )
}
