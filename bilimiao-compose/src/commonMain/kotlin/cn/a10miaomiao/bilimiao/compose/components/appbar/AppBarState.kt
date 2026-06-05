package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPageMenu

/**
 * AppBar 状态管理
 * 用于在 Compose 侧控制 AppBar 的显示和属性
 */
class AppBarState {
    // 是否显示 AppBar
    var visible by mutableStateOf(true)
        internal set

    // 标题
    var title by mutableStateOf<String?>(null)
        internal set

    // 是否显示返回按钮
    var canBack by mutableStateOf(false)
        internal set

    // 显示指针图标
    var showPointer by mutableStateOf(false)
        internal set

    // 指针方向
    var pointerOrientation by mutableStateOf(true)
        internal set

    // 显示交换图标
    var showExchange by mutableStateOf(false)
        internal set

    // 菜单列表
    var menus by mutableStateOf<List<MenuItemData>>(emptyList())
        internal set

    // 是否为导航菜单模式
    var isNavigationMenu by mutableStateOf(false)
        internal set

    // 当前选中的菜单项key
    var checkedKey by mutableStateOf<Int?>(null)
        internal set

    // 布局方向
    var orientation by mutableStateOf(AppBarOrientation.Vertical)
        internal set

    // 事件回调
    internal var _onBackClick: (() -> Unit)? = null
    internal var _onMenuClick: (() -> Unit)? = null
    internal var _onMenuItemClick: ((MenuItemData) -> Unit)? = null
    internal var _onPointerClick: (() -> Unit)? = null
    internal var _onExchangeClick: (() -> Unit)? = null

    // 底栏是否显示（用于竖屏模式）
    var barVisible by mutableStateOf(true)
        internal set

    // 菜单是否展开（用于竖屏模式）
    var menuExpanded by mutableStateOf(true)
        internal set

    var activeRootMenuPath by mutableStateOf<String?>(null)
        private set

    private val expandedMenuPaths = mutableStateListOf<String>()

    val hasExpandedSubMenus: Boolean
        get() = expandedMenuPaths.isNotEmpty()

    // 设置返回按钮点击事件
    fun setOnBackClickListener(listener: () -> Unit) {
        _onBackClick = listener
    }

    // 设置菜单按钮点击事件
    fun setOnMenuClickListener(listener: () -> Unit) {
        _onMenuClick = listener
    }

    // 设置菜单项点击事件
    fun setOnMenuItemClickListener(listener: (MenuItemData) -> Unit) {
        _onMenuItemClick = listener
    }

    // 设置指针点击事件
    fun setOnPointerClickListener(listener: () -> Unit) {
        _onPointerClick = listener
    }

    // 设置交换按钮点击事件
    fun setOnExchangeClickListener(listener: () -> Unit) {
        _onExchangeClick = listener
    }

    // 显示/隐藏底栏
    fun showBar() {
        barVisible = true
    }

    fun hideBar() {
        barVisible = false
    }

    // 显示/隐藏菜单
    fun showMenu() {
        menuExpanded = true
    }

    fun hideMenu() {
        menuExpanded = false
    }

    fun isRootMenuActive(path: List<Int>): Boolean {
        return activeRootMenuPath == path.toMenuPathKey()
    }

    fun toggleRootMenu(path: List<Int>) {
        val key = path.toMenuPathKey()
        if (activeRootMenuPath == key) {
            closeRootMenu()
            return
        }
        activeRootMenuPath = key
        expandedMenuPaths.clear()
    }

    fun closeRootMenu() {
        activeRootMenuPath = null
        expandedMenuPaths.clear()
    }

    fun isSubMenuExpanded(path: List<Int>): Boolean {
        return expandedMenuPaths.lastOrNull() == path.toMenuPathKey()
    }

    fun toggleSubMenu(path: List<Int>) {
        if (activeRootMenuPath == null) {
            return
        }
        val key = path.toMenuPathKey()
        val parentKey = path.dropLast(2).toMenuPathKey()
        trimExpandedMenusTo(parentKey)
        if (expandedMenuPaths.lastOrNull() == key) {
            expandedMenuPaths.removeAt(expandedMenuPaths.lastIndex)
        } else {
            expandedMenuPaths.add(key)
        }
    }

    fun getCurrentMenuParentPath(): List<Int>? {
        return (expandedMenuPaths.lastOrNull() ?: activeRootMenuPath)?.toMenuPathList()
    }

    fun navigateBackMenuLevel() {
        if (expandedMenuPaths.isNotEmpty()) {
            expandedMenuPaths.removeAt(expandedMenuPaths.lastIndex)
        } else {
            closeRootMenu()
        }
    }

    fun clearExpandedMenus() {
        closeRootMenu()
    }

    fun syncExpandedMenusWith(menus: List<MenuItemData>) {
        val validPaths = buildSet {
            fun collect(items: List<MenuItemData>, parentPath: List<Int>) {
                items.forEachIndexed { index, item ->
                    if (!item.childMenu.isNullOrEmpty()) {
                        val path = parentPath + item.key + index
                        add(path.toMenuPathKey())
                        collect(item.childMenu, path)
                    }
                }
            }
            collect(menus, emptyList())
        }
        val activeRootPath = activeRootMenuPath
        if (activeRootPath == null) {
            expandedMenuPaths.clear()
            return
        }
        if (activeRootPath !in validPaths) {
            closeRootMenu()
            return
        }
        val validExpandedPaths = mutableListOf<String>()
        var expectedParent = activeRootPath
        expandedMenuPaths.forEach { path ->
            val actualParent = path.toMenuPathList().dropLast(2).toMenuPathKey()
            if (path !in validPaths || actualParent != expectedParent) {
                return@forEach
            }
            validExpandedPaths += path
            expectedParent = path
        }
        expandedMenuPaths.clear()
        expandedMenuPaths.addAll(validExpandedPaths)
    }

    // 重置状态
    fun clear() {
        title = null
        canBack = false
        showPointer = false
        pointerOrientation = true
        showExchange = false
        menus = emptyList()
        isNavigationMenu = false
        checkedKey = null
        orientation = AppBarOrientation.Vertical
        barVisible = true
        menuExpanded = true
        clearExpandedMenus()
    }

    private fun trimExpandedMenusTo(parentKey: String) {
        if (parentKey.isEmpty()) {
            expandedMenuPaths.clear()
            return
        }
        val parentIndex = expandedMenuPaths.indexOf(parentKey)
        if (parentIndex == -1) {
            expandedMenuPaths.clear()
            return
        }
        while (expandedMenuPaths.size > parentIndex + 1) {
            expandedMenuPaths.removeAt(expandedMenuPaths.lastIndex)
        }
    }
}

private fun List<Int>.toMenuPathKey(): String {
    return joinToString(separator = "/")
}

private fun String.toMenuPathList(): List<Int> {
    if (isEmpty()) {
        return emptyList()
    }
    return split("/").map { it.toInt() }
}

/**
 * 记住 AppBarState
 */
@Composable
fun rememberAppBarState(): AppBarState {
    return remember {
        AppBarState()
    }
}

/**
 * CompositionLocal 用于在组件树中提供 AppBarState
 */
val LocalAppBarState: ProvidableCompositionLocal<AppBarState?> = compositionLocalOf { null }

/**
 * AppBar DSL 构建器
 */
class AppBarStateScope(
    private val state: AppBarState,
) {
    fun title(title: String?) {
        state.title = title
    }

    fun canBack(canBack: Boolean) {
        state.canBack = canBack
    }

    fun showPointer(show: Boolean, orientation: Boolean = true) {
        state.showPointer = show
        state.pointerOrientation = orientation
    }

    fun showExchange(show: Boolean) {
        state.showExchange = show
    }

//    fun menus(block: MyPageMenu.() -> Unit) {
//        val menu = MyPageMenu().apply(block)
//        state.menus = menu.items.map { MenuItemData.fromPropInfo(it) }
//    }

//    fun navigationMenu(checkedKey: Int? = null, block: MyPageMenu.() -> Unit) {
//        state.isNavigationMenu = true
//        state.checkedKey = checkedKey
//        val menu = MyPageMenu().apply(block)
//        state.menus = menu.items.map { MenuItemData.fromPropInfo(it) }
//    }

    fun orientation(orientation: AppBarOrientation) {
        state.orientation = orientation
    }

    fun onBackClick(listener: () -> Unit) {
        state.setOnBackClickListener(listener)
    }

    fun onMenuClick(listener: () -> Unit) {
        state.setOnMenuClickListener(listener)
    }

    fun onMenuItemClick(listener: (MenuItemData) -> Unit) {
        state.setOnMenuItemClickListener(listener)
    }

    fun onPointerClick(listener: () -> Unit) {
        state.setOnPointerClickListener(listener)
    }

    fun onExchangeClick(listener: () -> Unit) {
        state.setOnExchangeClickListener(listener)
    }
}

/**
 * AppBar 配置 DSL
 */
fun AppBarState.configure(block: AppBarStateScope.() -> Unit) {
    AppBarStateScope(this).block()
}

@Composable
fun PageAppBarEffect(
    showPointer: Boolean = false,
    pointerOrientation: Boolean = true,
    showExchange: Boolean = false,
    onPointerClick: (() -> Unit)? = null,
    onExchangeClick: (() -> Unit)? = null,
) {
    val appBarState = LocalAppBarState.current ?: return
    DisposableEffect(
        appBarState,
        showPointer,
        pointerOrientation,
        showExchange,
        onPointerClick,
        onExchangeClick,
    ) {
        appBarState.showPointer = showPointer
        appBarState.pointerOrientation = pointerOrientation
        appBarState.showExchange = showExchange
        appBarState.setOnPointerClickListener(onPointerClick ?: {})
        appBarState.setOnExchangeClickListener(onExchangeClick ?: {})
        onDispose {
            appBarState.showPointer = false
            appBarState.pointerOrientation = true
            appBarState.showExchange = false
            appBarState.setOnPointerClickListener {}
            appBarState.setOnExchangeClickListener {}
        }
    }
}
