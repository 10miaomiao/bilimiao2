package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 锚点在窗口坐标系中的矩形（px）。
 *
 * 一级菜单弹层与返回键长按网格面板共用：两者都需要「被点击元素在窗口中的真实位置」
 * 才能把弹层贴到锚点旁边，因此这里提升为同包可见的共享类型。
 */
internal data class MenuAnchor(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

private val VerticalRootPopupWidth = 168.dp

/**
 * 一级菜单弹层最小高度。
 *
 * 弹层高度上限来自真实可用空间，但当锚点贴近安全区边缘（例如 AppBar 贴着屏幕顶部）
 * 时可用空间会趋近 0。此时若直接 heightIn(max = 0.dp)，菜单列会被压成 0 高度而完全
 * 不可见（内部 verticalScroll 也失去意义），因此给一个「至少能看到两三个菜单项」的
 * 下限，再与窗口安全区可用高度一起 clamp，保证永远不会超出屏幕。
 */
private val RootPopupMinHeight = 120.dp

@Composable
internal fun VerticalAppBarMenus(
    menus: List<MenuItemData>,
    isNavigationMenu: Boolean,
    checkedKey: Int?,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anchors = remember { mutableStateMapOf<String, MenuAnchor>() }
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            reverseLayout = true,
        ) {
            items(
                items = menus,
                key = { it.key },
            ) { menuItem ->
                val index = menus.indexOf(menuItem)
                val itemPath = listOf(menuItem.key, index)
                VerticalTopLevelMenuItem(
                    data = menuItem,
                    menuCheckable = isNavigationMenu,
                    checked = checkedKey == menuItem.key,
                    appBarState = appBarState,
                    onMenuItemClick = onMenuItemClick,
                    path = itemPath,
                    onAnchorChanged = { anchors[itemPath.toPathKey()] = it },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        SharedRootMenuPopup(
            menus = menus,
            appBarState = appBarState,
            onMenuItemClick = onMenuItemClick,
            anchors = anchors,
            placement = AppBarPopupPlacement.AboveAppBar,
        )
    }
}

@Composable
internal fun HorizontalAppBarMenus(
    menus: List<MenuItemData>,
    isNavigationMenu: Boolean,
    checkedKey: Int?,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anchors = remember { mutableStateMapOf<String, MenuAnchor>() }
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = menus,
                key = { it.key },
            ) { menuItem ->
                val index = menus.indexOf(menuItem)
                val itemPath = listOf(menuItem.key, index)
                HorizontalTopLevelMenuItem(
                    data = menuItem,
                    menuCheckable = isNavigationMenu,
                    checked = checkedKey == menuItem.key,
                    appBarState = appBarState,
                    onMenuItemClick = onMenuItemClick,
                    path = itemPath,
                    onAnchorChanged = { anchors[itemPath.toPathKey()] = it },
                    modifier = Modifier.fillMaxWidth().animateItem(),
                )
            }
        }
        SharedRootMenuPopup(
            menus = menus,
            appBarState = appBarState,
            onMenuItemClick = onMenuItemClick,
            anchors = anchors,
            placement = AppBarPopupPlacement.RightOfAppBar,
        )
    }
}

@Composable
private fun VerticalTopLevelMenuItem(
    data: MenuItemData,
    menuCheckable: Boolean,
    checked: Boolean,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    path: List<Int>,
    onAnchorChanged: (MenuAnchor) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopLevelTreeMenuItem(
        data = data,
        menuCheckable = menuCheckable,
        checked = checked,
        appBarState = appBarState,
        onMenuItemClick = onMenuItemClick,
        path = path,
        onAnchorChanged = onAnchorChanged,
        modifier = modifier,
        vertical = true,
    )
}

@Composable
private fun HorizontalTopLevelMenuItem(
    data: MenuItemData,
    menuCheckable: Boolean,
    checked: Boolean,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    path: List<Int>,
    onAnchorChanged: (MenuAnchor) -> Unit,
    modifier: Modifier = Modifier,
) {
    TopLevelTreeMenuItem(
        data = data,
        menuCheckable = menuCheckable,
        checked = checked,
        appBarState = appBarState,
        onMenuItemClick = onMenuItemClick,
        path = path,
        onAnchorChanged = onAnchorChanged,
        modifier = modifier,
        vertical = false,
    )
}

@Composable
private fun TopLevelTreeMenuItem(
    data: MenuItemData,
    menuCheckable: Boolean,
    checked: Boolean,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    path: List<Int>,
    onAnchorChanged: (MenuAnchor) -> Unit,
    modifier: Modifier = Modifier,
    vertical: Boolean,
) {
    val hasChildren = !data.childMenu.isNullOrEmpty()
    val expanded = hasChildren && appBarState.isRootMenuActive(path)
    val onClick: (MenuItemData) -> Unit = {
        if (hasChildren) {
            appBarState.toggleRootMenu(path)
        } else {
            appBarState.clearExpandedMenus()
            onMenuItemClick(it)
        }
    }
    val anchorModifier = modifier.onGloballyPositioned { coordinates ->
        val bounds = coordinates.boundsInWindow()
        onAnchorChanged(
            MenuAnchor(
                x = bounds.left.roundToInt(),
                y = bounds.top.roundToInt(),
                width = bounds.width.roundToInt(),
                height = bounds.height.roundToInt(),
            )
        )
    }
    if (vertical) {
        VerticalTopLevelAppBarTreeMenuItem(
            data = data,
            checked = checked,
            expandable = hasChildren,
            expanded = expanded,
            onClick = onClick,
            modifier = anchorModifier,
            checkable = menuCheckable,
        )
    } else {
        HorizontalTopLevelAppBarTreeMenuItem(
            data = data,
            checked = checked,
            expandable = hasChildren,
            expanded = expanded,
            onClick = onClick,
            modifier = anchorModifier,
            checkable = menuCheckable,
        )
    }
}

@Composable
private fun SharedRootMenuPopup(
    menus: List<MenuItemData>,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    anchors: Map<String, MenuAnchor>,
    placement: AppBarPopupPlacement,
) {
    val activeRootPath = appBarState.activeRootMenuPath
    var renderedRootPath by remember { mutableStateOf<String?>(null) }
    var renderedParentPath by remember { mutableStateOf<List<Int>?>(null) }
    val visibleState = remember { MutableTransitionState(false) }

    if (activeRootPath != null) {
        val currentParentPath = appBarState.getCurrentMenuParentPath()
        if (currentParentPath == null) {
            LaunchedEffect(activeRootPath) {
                appBarState.closeRootMenu()
            }
            return
        }
        renderedRootPath = activeRootPath
        renderedParentPath = currentParentPath
        visibleState.targetState = true
    } else {
        visibleState.targetState = false
    }

    val popupRootPath = renderedRootPath ?: return
    val popupParentPath = renderedParentPath ?: return
    if (!visibleState.currentState && !visibleState.targetState) {
        return
    }

    val popupParentItem = findMenuItemByPath(menus, popupParentPath) ?: run {
        LaunchedEffect(popupRootPath, popupParentPath) {
            appBarState.closeRootMenu()
        }
        return
    }
    val popupMenus = popupParentItem.childMenu.orEmpty()
    if (popupMenus.isEmpty()) {
        LaunchedEffect(popupRootPath, popupParentPath) {
            appBarState.closeRootMenu()
        }
        return
    }

    val activeAnchor = anchors[popupRootPath]
    if (activeAnchor == null) {
        LaunchedEffect(popupRootPath, activeAnchor, placement) {
            appBarState.closeRootMenu()
        }
        return
    }

    val density = LocalDensity.current
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val safeTopPx = with(density) { safePadding.calculateTopPadding().roundToPx() }
    val safeBottomPx = with(density) { safePadding.calculateBottomPadding().roundToPx() }
    val safeLeftPx = with(density) { safePadding.calculateLeftPadding(LayoutDirection.Ltr).roundToPx() }
    val safeRightPx = with(density) { safePadding.calculateRightPadding(LayoutDirection.Ltr).roundToPx() }
    val popupWidthDp = when (placement) {
        AppBarPopupPlacement.AboveAppBar -> VerticalRootPopupWidth
        AppBarPopupPlacement.RightOfAppBar -> AppBarConfig.MenuWidth
    }
    // Popup 定位回调实测到的「弹层所在窗口」高度（px）。
    // 0 表示尚未测得（首帧），调用方必须把它当成「未知」显式处理，绝不能当哨兵参与换算。
    // key 只绑定 placement：进入子菜单时 popupParentPath 会变，但窗口高度并没有变，
    // 若把它放进 key 会重建一个 0 值状态，而 popupPositionProvider 捕获的仍是旧状态对象，
    // 于是「实测窗口高度」这一支路会静默失效（只能靠 LocalWindowInfo 兜底）。
    var measuredWindowHeightPx by remember(placement) { mutableIntStateOf(0) }

    val popupPositionProvider = remember(
        placement,
        activeAnchor,
        safeTopPx,
        safeBottomPx,
        safeLeftPx,
        safeRightPx,
    ) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: androidx.compose.ui.unit.IntRect,
                windowSize: androidx.compose.ui.unit.IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: androidx.compose.ui.unit.IntSize,
            ): IntOffset {
                // 记录弹层所在窗口的真实高度，供后续帧计算最大高度（只在变化时写入，避免多余重组）
                if (measuredWindowHeightPx != windowSize.height) {
                    measuredWindowHeightPx = windowSize.height
                }
                val actualPopupWidth = popupContentSize.width
                val actualPopupHeight = popupContentSize.height
                return when (placement) {
                    AppBarPopupPlacement.AboveAppBar -> {
                        val anchorCenterX = activeAnchor.x + activeAnchor.width / 2
                        val preferredLeft = anchorCenterX - actualPopupWidth / 2
                        val maxLeft = max(windowSize.width - safeRightPx - actualPopupWidth, safeLeftPx)
                        IntOffset(
                            x = preferredLeft.coerceIn(safeLeftPx, maxLeft),
                            y = max(activeAnchor.y - actualPopupHeight, safeTopPx),
                        )
                    }
                    AppBarPopupPlacement.RightOfAppBar -> {
                        val anchorCenterY = activeAnchor.y + activeAnchor.height / 2
                        val preferredTop = anchorCenterY - actualPopupHeight / 2
                        val maxTop = max(windowSize.height - safeBottomPx - actualPopupHeight, safeTopPx)
                        IntOffset(
                            x = activeAnchor.x + activeAnchor.width,
                            y = preferredTop.coerceIn(safeTopPx, maxTop),
                        )
                    }
                }
            }
        }
    }

    // 弹层最大高度只能来自真实可用空间：
    // ① 优先用 Popup 定位回调实测到的窗口高度（与 Popup 的坐标系一致，最准确）；
    // ② 首帧还没测得时回退到 LocalWindowInfo 的窗口高度；
    // ③ 仍然拿不到（例如桌面端窗口尚未测量）时返回 null —— 显式表示「未知」，
    //    此时不给 heightIn 传任何限制，由布局自身按窗口约束测量。
    // 旧实现把 Int.MAX_VALUE 当作「窗口高度未知」的哨兵，而窗口高度要到
    // calculatePosition（内容测量之后）才写入，于是首帧直接拿 Int.MAX_VALUE 去算
    // heightIn(max = maxHeightPx.toDp())：dp→px 往返换算后得到 ≈2^31 的像素值
    // （实测设备上是 2147483520），它远超 Constraints 能打包的尺寸上限，
    // 抛出 "Can't represent a width of 0 and height of 2147483520 in Constraints"。
    val windowHeightPx = measuredWindowHeightPx.takeIf { it > 0 }
        ?: LocalWindowInfo.current.containerSize.height.takeIf { it > 0 }
    val popupMaxHeightDp = resolveRootPopupMaxHeight(
        placement = placement,
        anchorTopPx = activeAnchor.y,
        windowHeightPx = windowHeightPx,
        safeTopPx = safeTopPx,
        safeBottomPx = safeBottomPx,
        density = density,
    )

    val popupTransformOrigin = remember(placement) {
        when (placement) {
            AppBarPopupPlacement.AboveAppBar -> TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1f)
            AppBarPopupPlacement.RightOfAppBar -> TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
        }
    }

    Popup(
        popupPositionProvider = popupPositionProvider,
        onDismissRequest = appBarState::closeRootMenu,
        properties = PopupProperties(focusable = true),
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn() + scaleIn(
                initialScale = 0.82f,
                transformOrigin = popupTransformOrigin,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 520f,
                ),
            ),
            exit = fadeOut() + scaleOut(
                targetScale = 0.9f,
                transformOrigin = popupTransformOrigin,
                animationSpec = spring(
                    dampingRatio = 0.9f,
                    stiffness = 700f,
                ),
            ),
        ) {
            MiaoOutlinedCard{
                Column(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .width(popupWidthDp)
                        // Dp.Unspecified 表示窗口高度未知，此时不给高度限制
                        .heightIn(max = popupMaxHeightDp ?: Dp.Unspecified)
                        .verticalScroll(rememberScrollState()),
                ) {
                    if (appBarState.hasExpandedSubMenus) {
                        MenuPanelBackButton(
                            onClick = { appBarState.navigateBackMenuLevel() }
                        )
                        HorizontalDivider()
                    }
                    popupMenus.forEachIndexed { index, menuItem ->
                        val itemPath = popupParentPath + menuItem.key + index
                        val hasChildren = !menuItem.childMenu.isNullOrEmpty()
                        val expanded = hasChildren && appBarState.isSubMenuExpanded(itemPath)
                        val onClick: (MenuItemData) -> Unit = {
                            if (hasChildren) {
                                appBarState.toggleSubMenu(itemPath)
                            } else {
                                appBarState.clearExpandedMenus()
                                onMenuItemClick(it)
                            }
                        }
                        if (popupParentItem.checkable) {
                            AppBarTreeCheckableMenuItem(
                                data = menuItem,
                                checked = popupParentItem.checkedKey == menuItem.key,
                                expandable = hasChildren,
                                expanded = expanded,
                                onClick = onClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            AppBarTreeMenuItem(
                                data = menuItem,
                                expandable = hasChildren,
                                expanded = expanded,
                                onClick = onClick,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuPanelBackButton(
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = onClick,
            )
            .padding(
                start = AppBarConfig.NavigationPadding,
                end = AppBarConfig.NavigationPadding,
                top = AppBarConfig.NavigationPadding,
                bottom = AppBarConfig.NavigationPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "返回上一级",
            modifier = Modifier.size(AppBarConfig.NavigationIconSize),
        )
        Text(
            text = "返回上一级",
            fontSize = AppBarConfig.TitleTextSize.value.sp,
        )
    }
}

/**
 * 计算 AppBar 弹层的最大高度（一级菜单弹层与返回键长按网格面板共用，全仓只此一份实现）。
 *
 * 返回值语义：
 * - `null` 表示「不做高度限制」：窗口高度未知，或窗口安全区内已无可用高度；
 * - 非 `null` 时一定是有限值（不会出现无穷大/溢出值），且不超过窗口安全区可用高度；
 *   唯一例外是 [AppBarPopupPlacement.AboveAppBar] 的退化分支——锚点上方空间小于
 *   [RootPopupMinHeight] 时会抬到该下限（仍受窗口安全区可用高度约束）。
 *
 * @param placement 弹层相对 AppBar 的放置方式
 * @param anchorTopPx 锚点（被点击的菜单项 / 返回键）在窗口坐标中的上边缘
 * @param windowHeightPx 弹层所在窗口高度，null 或 <= 0 表示未知
 * @param safeTopPx 安全区上内边距
 * @param safeBottomPx 安全区下内边距
 * @param density 用于 px↔dp 换算
 */
internal fun resolveRootPopupMaxHeight(
    placement: AppBarPopupPlacement,
    anchorTopPx: Int,
    windowHeightPx: Int?,
    safeTopPx: Int,
    safeBottomPx: Int,
    density: Density,
): Dp? {
    // 窗口安全区内的可用高度；窗口高度未知时按「未知」处理，不做限制
    val windowAvailable = windowHeightPx
        ?.takeIf { it > 0 }
        ?.let { heightPx ->
            with(density) {
                (heightPx.toDp() - safeTopPx.toDp() - safeBottomPx.toDp()).coerceAtLeast(0.dp)
            }
        }
        ?.takeIf { it > 0.dp }

    return when (placement) {
        AppBarPopupPlacement.AboveAppBar -> {
            // 锚点上方到安全区顶部的空间；AppBar 贴近安全区顶部时会退化成 0
            val spaceAbove = with(density) {
                (anchorTopPx.toDp() - safeTopPx.toDp()).coerceAtLeast(0.dp)
            }
            // 退化时不写 heightIn(max = 0.dp)（那会把菜单压成不可见），而是抬到最小可用高度，
            // 再与窗口安全区可用高度一起 clamp，保证既不超出屏幕、也至少能看到几个菜单项。
            // 抬到下限时弹层底部会盖住被点击的菜单项（定位仍限制在安全区内），这是可接受的降级。
            spaceAbove.coerceAtLeast(RootPopupMinHeight)
                .let { limit -> windowAvailable?.let { limit.coerceAtMost(it) } ?: limit }
        }
        // 横向 AppBar：菜单在侧边栏，弹层整列显示，可用高度就是整个安全区高度
        AppBarPopupPlacement.RightOfAppBar -> windowAvailable
    }
}

private fun findMenuItemByPath(
    menus: List<MenuItemData>,
    path: List<Int>,
): MenuItemData? {
    var currentMenus = menus
    var currentItem: MenuItemData? = null
    path.chunked(2).forEach { chunk ->
        if (chunk.size < 2) {
            return null
        }
        val key = chunk[0]
        val index = chunk[1]
        val item = currentMenus.getOrNull(index) ?: return null
        if (item.key != key) {
            return null
        }
        currentItem = item
        currentMenus = item.childMenu.orEmpty()
    }
    return currentItem
}

private fun List<Int>.toPathKey(): String {
    return joinToString(separator = "/")
}
