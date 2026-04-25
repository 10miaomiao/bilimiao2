package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

private data class MenuAnchor(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

@Composable
internal fun VerticalAppBarMenus(
    menus: List<MenuItemData>,
    isNavigationMenu: Boolean,
    checkedKey: Int?,
    themeColor: androidx.compose.ui.graphics.Color,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anchors = remember { mutableStateMapOf<String, MenuAnchor>() }
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            menus.forEachIndexed { index, menuItem ->
                val itemPath = listOf(menuItem.key, index)
                TopLevelMenuItem(
                    data = menuItem,
                    menuCheckable = isNavigationMenu,
                    checked = checkedKey == menuItem.key,
                    themeColor = themeColor,
                    appBarState = appBarState,
                    onMenuItemClick = onMenuItemClick,
                    path = itemPath,
                    onAnchorChanged = { anchors[itemPath.toPathKey()] = it },
                )
            }
        }
        menus.forEachIndexed { index, menuItem ->
            val itemPath = listOf(menuItem.key, index)
            if (appBarState.isMenuExpanded(itemPath)) {
                MenuPopupCascade(
                    menus = menuItem.childMenu.orEmpty(),
                    menuCheckable = menuItem.checkable,
                    checkedKey = menuItem.checkedKey,
                    themeColor = themeColor,
                    appBarState = appBarState,
                    onMenuItemClick = onMenuItemClick,
                    parentPath = itemPath,
                    anchor = anchors[itemPath.toPathKey()],
                    anchors = anchors,
                    showBelowAnchor = true,
                    popupDepth = 1,
                )
            }
        }
    }
}

@Composable
internal fun HorizontalAppBarMenus(
    menus: List<MenuItemData>,
    isNavigationMenu: Boolean,
    checkedKey: Int?,
    themeColor: androidx.compose.ui.graphics.Color,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anchors = remember { mutableStateMapOf<String, MenuAnchor>() }
    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            menus.forEachIndexed { index, menuItem ->
                val itemPath = listOf(menuItem.key, index)
                TopLevelMenuItem(
                    data = menuItem,
                    menuCheckable = isNavigationMenu,
                    checked = checkedKey == menuItem.key,
                    themeColor = themeColor,
                    appBarState = appBarState,
                    onMenuItemClick = onMenuItemClick,
                    path = itemPath,
                    onAnchorChanged = { anchors[itemPath.toPathKey()] = it },
                    modifier = Modifier.fillMaxWidth(),
                    verticalLayout = true,
                )
            }
        }
        menus.forEachIndexed { index, menuItem ->
            val itemPath = listOf(menuItem.key, index)
            if (appBarState.isMenuExpanded(itemPath)) {
                MenuPopupCascade(
                    menus = menuItem.childMenu.orEmpty(),
                    menuCheckable = menuItem.checkable,
                    checkedKey = menuItem.checkedKey,
                    themeColor = themeColor,
                    appBarState = appBarState,
                    onMenuItemClick = onMenuItemClick,
                    parentPath = itemPath,
                    anchor = anchors[itemPath.toPathKey()],
                    anchors = anchors,
                    showBelowAnchor = false,
                    popupDepth = 1,
                )
            }
        }
    }
}

@Composable
private fun TopLevelMenuItem(
    data: MenuItemData,
    menuCheckable: Boolean,
    checked: Boolean,
    themeColor: androidx.compose.ui.graphics.Color,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    path: List<Int>,
    onAnchorChanged: (MenuAnchor) -> Unit,
    modifier: Modifier = Modifier,
    verticalLayout: Boolean = false,
) {
    val hasChildren = !data.childMenu.isNullOrEmpty()
    val expanded = hasChildren && appBarState.isMenuExpanded(path)
    val onClick: (MenuItemData) -> Unit = {
        if (hasChildren) {
            appBarState.toggleMenuExpanded(path)
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
    if (menuCheckable) {
        CheckableMenuItem(
            data = data,
            checked = checked,
            themeColor = themeColor,
            onClick = onClick,
            modifier = anchorModifier,
            expandable = hasChildren,
            expanded = expanded,
            verticalLayout = verticalLayout,
        )
    } else {
        MenuItem(
            data = data,
            onClick = onClick,
            modifier = anchorModifier,
            expandable = hasChildren,
            expanded = expanded,
            verticalLayout = verticalLayout,
        )
    }
}

@Composable
private fun MenuPopupCascade(
    menus: List<MenuItemData>,
    menuCheckable: Boolean,
    checkedKey: Int?,
    themeColor: androidx.compose.ui.graphics.Color,
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    parentPath: List<Int>,
    anchor: MenuAnchor?,
    anchors: MutableMap<String, MenuAnchor>,
    showBelowAnchor: Boolean,
    popupDepth: Int,
) {
    if (menus.isEmpty() || anchor == null) {
        return
    }
    val popupOffset = if (showBelowAnchor) {
        IntOffset(anchor.x, anchor.y + anchor.height)
    } else {
        IntOffset(anchor.x + anchor.width, anchor.y)
    }
    Popup(
        offset = popupOffset,
        onDismissRequest = {
            if (popupDepth == 1) {
                appBarState.clearExpandedMenus()
            }
        },
        properties = PopupProperties(
            focusable = popupDepth == 1,
        ),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = AppBarConfig.DividerHeight,
            shadowElevation = AppBarConfig.NavigationIconPadding,
        ) {
            Box {
                Column(modifier = Modifier.width(AppBarConfig.MenuWidth)) {
                    menus.forEachIndexed { index, menuItem ->
                        val itemPath = parentPath + menuItem.key + index
                        val hasChildren = !menuItem.childMenu.isNullOrEmpty()
                        val expanded = hasChildren && appBarState.isMenuExpanded(itemPath)
                        val onClick: (MenuItemData) -> Unit = {
                            if (hasChildren) {
                                appBarState.toggleMenuExpanded(itemPath)
                            } else {
                                appBarState.clearExpandedMenus()
                                onMenuItemClick(it)
                            }
                        }
                        val itemModifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                val bounds = coordinates.boundsInWindow()
                                anchors[itemPath.toPathKey()] = MenuAnchor(
                                    x = bounds.left.roundToInt(),
                                    y = bounds.top.roundToInt(),
                                    width = bounds.width.roundToInt(),
                                    height = bounds.height.roundToInt(),
                                )
                            }
                        if (menuCheckable) {
                            CheckableMenuItem(
                                data = menuItem,
                                checked = checkedKey == menuItem.key,
                                themeColor = themeColor,
                                onClick = onClick,
                                modifier = itemModifier,
                                expandable = hasChildren,
                                expanded = expanded,
                                verticalLayout = true,
                            )
                        } else {
                            MenuItem(
                                data = menuItem,
                                onClick = onClick,
                                modifier = itemModifier,
                                expandable = hasChildren,
                                expanded = expanded,
                                verticalLayout = true,
                            )
                        }
                    }
                }
                menus.forEachIndexed { index, menuItem ->
                    val itemPath = parentPath + menuItem.key + index
                    if (appBarState.isMenuExpanded(itemPath)) {
                        MenuPopupCascade(
                            menus = menuItem.childMenu.orEmpty(),
                            menuCheckable = menuItem.checkable,
                            checkedKey = menuItem.checkedKey,
                            themeColor = themeColor,
                            appBarState = appBarState,
                            onMenuItemClick = onMenuItemClick,
                            parentPath = itemPath,
                            anchor = anchors[itemPath.toPathKey()],
                            anchors = anchors,
                            showBelowAnchor = false,
                            popupDepth = popupDepth + 1,
                        )
                    }
                }
            }
        }
    }
}

private fun List<Int>.toPathKey(): String {
    return joinToString(separator = "/")
}
