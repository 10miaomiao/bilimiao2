package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 可折叠 AppBar 状态
 */
class CollapsingAppBarState {
    var scrollOffset: Float by mutableFloatStateOf(0f)
        internal set

    var isCollapsed: Boolean by mutableStateOf(false)
        internal set

    val nestedScrollConnection: NestedScrollConnection
        get() = object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset = Offset(0)

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = Offset(0)
        }

    fun expand(scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            scrollOffset = 0f
            isCollapsed = false
        }
    }

    fun collapse(scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch {
            val maxOffset = (AppBarConfig.Height - AppBarConfig.MenuHeight).value
            scrollOffset = maxOffset
            isCollapsed = true
        }
    }
}

@Composable
fun rememberCollapsingAppBarState(): CollapsingAppBarState {
    return remember {
        CollapsingAppBarState()
    }
}

/**
 * 可折叠 AppBar
 * 支持竖屏布局，可随内容滚动隐藏/显示
 *
 * @param state 滚动状态
 * @param title 标题
 * @param canBack 是否显示返回按钮
 * @param menus 菜单列表
 * @param isNavigationMenu 是否为导航菜单模式
 * @param checkedKey 当前选中的菜单项key
 * @param themeColor 主题色
 * @param backgroundColor 背景色
 * @param onBackClick 返回按钮点击
 * @param onMenuClick 菜单按钮点击
 * @param onMenuItemClick 菜单项点击
 * @param modifier 修饰符
 * @param content 内容区域（会随 AppBar 滚动）
 */
@Composable
fun CollapsingAppBar(
    state: CollapsingAppBarState,
    title: String?,
    canBack: Boolean = false,
    menus: List<MenuItemData> = emptyList(),
    isNavigationMenu: Boolean = false,
    checkedKey: Int? = null,
    themeColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onBackClick: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onMenuItemClick: (MenuItemData) -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    // 标题透明度动画
    val titleAlpha by animateFloatAsState(
        targetValue = if (state.isCollapsed) 0f else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "titleAlpha",
    )

    // AppBar 整体偏移
    val offsetY by animateFloatAsState(
        targetValue = state.scrollOffset,
        animationSpec = tween(
            durationMillis = if (state.isCollapsed) 175 else 225,
            easing = LinearEasing
        ),
        label = "offsetY",
    )

    val contentColor = LocalContentColor.current

    Column(
        modifier = modifier
    ) {
        // 可折叠区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, -offsetY.roundToInt()) }
        ) {
            Column {
                // 标题区域（滚动时淡出）
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppBarConfig.TitleHeight)
                        .alpha(titleAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!title.isNullOrEmpty()) {
                        Text(
                            text = title.replace("\n", " "),
                            color = contentColor.copy(alpha = 0.45f),
                            fontSize = AppBarConfig.TitleTextSize.value.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // 菜单行
                CollapsingAppBarMenuRow(
                    canBack = canBack,
                    menus = menus,
                    isNavigationMenu = isNavigationMenu,
                    checkedKey = checkedKey,
                    themeColor = themeColor,
                    onBackClick = {
                        if (state.isCollapsed) {
                            state.expand(scope)
                        }
                        onBackClick()
                    },
                    onMenuClick = onMenuClick,
                    onMenuItemClick = onMenuItemClick,
                )
            }

            // 底部渐变遮罩
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppBarConfig.TitleHeight)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                backgroundColor,
                            )
                        )
                    )
                    .alpha(1f - titleAlpha)
            )
        }

        // 分割线
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = AppBarConfig.DividerHeight,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        // 内容区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
    }
}

/**
 * 可折叠 AppBar 的菜单行
 */
@Composable
private fun CollapsingAppBarMenuRow(
    canBack: Boolean,
    menus: List<MenuItemData>,
    isNavigationMenu: Boolean,
    checkedKey: Int?,
    themeColor: Color,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit,
    onMenuItemClick: (MenuItemData) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AppBarConfig.MenuHeight)
            .padding(horizontal = AppBarConfig.NavigationIconPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 导航图标
        NavigationIcon(
            icon = if (canBack) AppBarNavigationIcon.Back else AppBarNavigationIcon.Menu,
            onClick = if (canBack) onBackClick else onMenuClick,
        )

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
