package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import kotlin.math.max
import kotlin.math.roundToInt

private data class MenuAnchor(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

private enum class RootPopupPlacement {
    AboveAppBar,
    RightOfAppBar,
}

private val VerticalRootPopupWidth = 168.dp

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
        val reversedMenu = menus.asReversed()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.End,
        ) {
            reversedMenu.forEachIndexed { index, menuItem ->
                val itemPath = listOf(menuItem.key, index)
                VerticalTopLevelMenuItem(
                    data = menuItem,
                    menuCheckable = isNavigationMenu,
                    checked = checkedKey == menuItem.key,
                    appBarState = appBarState,
                    onMenuItemClick = onMenuItemClick,
                    path = itemPath,
                    onAnchorChanged = { anchors[itemPath.toPathKey()] = it },
                )
            }
        }
        SharedRootMenuPopup(
            menus = reversedMenu,
            appBarState = appBarState,
            onMenuItemClick = onMenuItemClick,
            anchors = anchors,
            placement = RootPopupPlacement.AboveAppBar,
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
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            menus.forEachIndexed { index, menuItem ->
                val itemPath = listOf(menuItem.key, index)
                HorizontalTopLevelMenuItem(
                    data = menuItem,
                    menuCheckable = isNavigationMenu,
                    checked = checkedKey == menuItem.key,
                    appBarState = appBarState,
                    onMenuItemClick = onMenuItemClick,
                    path = itemPath,
                    onAnchorChanged = { anchors[itemPath.toPathKey()] = it },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        SharedRootMenuPopup(
            menus = menus,
            appBarState = appBarState,
            onMenuItemClick = onMenuItemClick,
            anchors = anchors,
            placement = RootPopupPlacement.RightOfAppBar,
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
    placement: RootPopupPlacement,
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
    val configuration = LocalConfiguration.current
    val safePadding = WindowInsets.safeDrawing.asPaddingValues()
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }
    val safeTopPx = with(density) { safePadding.calculateTopPadding().roundToPx() }
    val safeBottomPx = with(density) { safePadding.calculateBottomPadding().roundToPx() }
    val safeLeftPx = with(density) { safePadding.calculateLeftPadding(LayoutDirection.Ltr).roundToPx() }
    val safeRightPx = with(density) { safePadding.calculateRightPadding(LayoutDirection.Ltr).roundToPx() }
    val popupWidthDp = when (placement) {
        RootPopupPlacement.AboveAppBar -> VerticalRootPopupWidth
        RootPopupPlacement.RightOfAppBar -> AppBarConfig.MenuWidth
    }
    var popupHeightPx by remember(popupRootPath, popupParentPath, placement) { mutableIntStateOf(0) }

    val maxHeightPx = when (placement) {
        RootPopupPlacement.AboveAppBar -> max(activeAnchor.y - safeTopPx, 0)
        RootPopupPlacement.RightOfAppBar -> max(screenHeightPx - safeTopPx - safeBottomPx, 0)
    }
    val popupPositionProvider = remember(
        placement,
        activeAnchor,
        safeTopPx,
        safeBottomPx,
        safeLeftPx,
        safeRightPx,
        screenWidthPx,
        screenHeightPx,
    ) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: androidx.compose.ui.unit.IntRect,
                windowSize: androidx.compose.ui.unit.IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: androidx.compose.ui.unit.IntSize,
            ): IntOffset {
                val actualPopupWidth = popupContentSize.width
                val actualPopupHeight = popupContentSize.height
                return when (placement) {
                    RootPopupPlacement.AboveAppBar -> {
                        val anchorCenterX = activeAnchor.x + activeAnchor.width / 2
                        val preferredLeft = anchorCenterX - actualPopupWidth / 2
                        val maxLeft = max(windowSize.width - safeRightPx - actualPopupWidth, safeLeftPx)
                        IntOffset(
                            x = preferredLeft.coerceIn(safeLeftPx, maxLeft),
                            y = max(activeAnchor.y - actualPopupHeight, safeTopPx),
                        )
                    }
                    RootPopupPlacement.RightOfAppBar -> {
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

    val popupTransformOrigin = remember(placement) {
        when (placement) {
            RootPopupPlacement.AboveAppBar -> TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1f)
            RootPopupPlacement.RightOfAppBar -> TransformOrigin(pivotFractionX = 0f, pivotFractionY = 0.5f)
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
                        .heightIn(max = with(density) { maxHeightPx.toDp() })
                        .onGloballyPositioned { popupHeightPx = it.size.height }
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
            painter = painterResource(id = R.drawable.ic_back_24dp),
            contentDescription = "返回上一级",
            modifier = Modifier.size(AppBarConfig.NavigationIconSize),
        )
        Text(
            text = "返回上一级",
            fontSize = AppBarConfig.TitleTextSize.value.sp,
        )
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

private fun String.toPathList(): List<Int> {
    if (isEmpty()) {
        return emptyList()
    }
    return split("/").map { it.toInt() }
}
