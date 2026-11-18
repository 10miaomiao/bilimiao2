package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * 返回/菜单按钮的点击区域宽度。
 *
 * 只在 [NavigationIcon] 内部使用，刻意比 [AppBarConfig.MenuItemMinWidth]（60dp）小一档：
 * 图标区域视觉略收、但仍是 56×46dp 的足够大点击区。
 * 不要改成直接修改 [AppBarConfig]——那里的 `MenuHeight` / `MenuItemMinWidth` / `NavigationIconSize`
 * 被 AppBar 行高、收缩动画位移、菜单弹层返回行等多处复用，改动会牵动全应用。
 */
private val AppBarNavButtonWidth = 56.dp

/** 返回/菜单按钮的点击区域高度（比 [AppBarConfig.MenuHeight] 小一档，原因见 [AppBarNavButtonWidth]） */
private val AppBarNavButtonHeight = 46.dp

/** 返回/菜单按钮的图标尺寸（比 [AppBarConfig.NavigationIconSize] 小一档，让图标与收窄后的按钮比例一致） */
private val AppBarNavButtonIconSize = 22.dp

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
    data.iconVector?.let {
        Icon(
            imageVector = it,
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

/**
 * AppBar 的返回/菜单按钮。
 *
 * @param onLongClick 长按回调（仅当 [icon] 是返回箭头、且 [longPressActions] 为空时触发）
 * @param longPressActions 返回键长按弹出的网格面板动作，非空时优先弹面板
 */
@Composable
fun NavigationIcon(
    icon: AppBarNavigationIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    longPressActions: List<AppBarBackAction> = emptyList(),
    longPressPlacement: AppBarPopupPlacement = AppBarPopupPlacement.AboveAppBar,
) {
    val contentColor = LocalContentColor.current
    val interactionSource = remember { MutableInteractionSource() }
    // 只有「返回箭头」状态才有长按语义：菜单图标状态下长按既不弹面板也不回调宿主
    val longPressEnabled = icon == AppBarNavigationIcon.Back
    // 自身在窗口中的矩形，长按面板据此贴边定位（每次布局都会刷新，打开前一定是最新值）
    var anchor by remember { mutableStateOf<MenuAnchor?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    // 两种失效条件都要立刻收起面板：
    // ① 动作列表变空（例如播放器关闭），否则下次动作恢复时面板会在没有任何长按操作的情况下突然出现；
    // ② 图标不再是返回箭头（canBack 变 false），否则面板会在关闭态累积「待弹出」状态，
    //    等用户进入有返回箭头的页面时无故自动弹出
    LaunchedEffect(longPressEnabled, longPressActions.isEmpty()) {
        if (!longPressEnabled || longPressActions.isEmpty()) {
            menuOpen = false
        }
    }

    val iconVector = when (icon) {
        AppBarNavigationIcon.Back -> Icons.AutoMirrored.Filled.ArrowBack
        AppBarNavigationIcon.Menu -> Icons.Filled.Menu
    }

    Box(
        modifier = modifier
            // 按钮视觉略收一档，但仍保留足够点击区域；
            // 横屏 [AppBarHorizontal] 传入的 Modifier.width(MenuWidth=120dp) 会把这层 size 的宽 coerce 到
            // 120dp，属于既有行为，这里不为它改变传参
            .size(AppBarNavButtonWidth, AppBarNavButtonHeight)
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInWindow()
                anchor = MenuAnchor(
                    x = bounds.left.roundToInt(),
                    y = bounds.top.roundToInt(),
                    width = bounds.width.roundToInt(),
                    height = bounds.height.roundToInt(),
                )
            }
            // 用 combinedClickable 而不是 clickable：返回箭头长按要么弹网格面板（有动作时），
            // 要么交给宿主的 onLongClick（例如「未播放时直接回首页」），点击行为保持不变。
            // 菜单图标状态下整体不挂长按（onLongClick 传 null 时 combinedClickable 退化为
            // 只响应点击），与老项目 bbmiao 一致：长按语义只属于返回箭头
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = onClick,
                onLongClick = if (longPressEnabled) {
                    {
                        if (longPressActions.isNotEmpty()) {
                            menuOpen = true
                        } else {
                            onLongClick()
                        }
                    }
                } else {
                    null
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = iconVector,
            contentDescription = when (icon) {
                AppBarNavigationIcon.Back -> "返回"
                AppBarNavigationIcon.Menu -> "菜单"
            },
            tint = contentColor,
            modifier = Modifier.size(AppBarNavButtonIconSize),
        )
    }

    // 锚点只在面板打开时才读取：关闭状态下不订阅 anchor 的变化，
    // 否则 AppBar 收缩时（graphicsLayer 逐帧位移会让 boundsInWindow 每帧变化）
    // 会逐帧重组本组件，抵消 AppBar「收缩不触发重组」的优化。
    // 打开的那一刻 anchor 一定是当前帧的最新值——onGloballyPositioned 每次布局都会写入
    AppBarBackLongPressPopup(
        visible = menuOpen,
        actions = longPressActions,
        anchor = if (menuOpen) anchor else null,
        placement = longPressPlacement,
        onDismiss = { menuOpen = false },
    )
}
