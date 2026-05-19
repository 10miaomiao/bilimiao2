package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anchors = remember { mutableStateMapOf<String, MenuAnchor>() }
    Box(modifier = modifier.fillMaxWidth()) {
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
        reversedMenu.forEachIndexed { index, menuItem ->
            val itemPath = listOf(menuItem.key, index)
            if (appBarState.isMenuExpanded(itemPath)) {
                MenuPopupCascade(
                    menus = menuItem.childMenu.orEmpty(),
                    menuCheckable = menuItem.checkable,
                    checkedKey = menuItem.checkedKey,
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
    appBarState: AppBarState,
    onMenuItemClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val anchors = remember { mutableStateMapOf<String, MenuAnchor>() }
    Box(modifier = modifier.fillMaxWidth()) {
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
        menus.forEachIndexed { index, menuItem ->
            val itemPath = listOf(menuItem.key, index)
            if (appBarState.isMenuExpanded(itemPath)) {
                MenuPopupCascade(
                    menus = menuItem.childMenu.orEmpty(),
                    menuCheckable = menuItem.checkable,
                    checkedKey = menuItem.checkedKey,
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
private fun MenuPopupCascade(
    menus: List<MenuItemData>,
    menuCheckable: Boolean,
    checkedKey: Int?,
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
            shadowElevation = AppBarConfig.NavigationPadding,
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
                            AppBarTreeCheckableMenuItem(
                                data = menuItem,
                                checked = checkedKey == menuItem.key,
                                expandable = hasChildren,
                                expanded = expanded,
                                onClick = onClick,
                                modifier = itemModifier,
                            )
                        } else {
                            AppBarTreeMenuItem(
                                data = menuItem,
                                expandable = hasChildren,
                                expanded = expanded,
                                onClick = onClick,
                                modifier = itemModifier,
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
