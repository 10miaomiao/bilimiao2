package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

    // 主题色
    var themeColor by mutableStateOf(Color.Unspecified)
        internal set

    // 背景色
    var backgroundColor by mutableStateOf(Color.Unspecified)
        internal set

    // 布局方向
    var orientation by mutableStateOf(AppBarOrientation.Vertical)
        internal set

    // 是否启用副内容（横屏时显示指针和交换按钮）
    var enableSubContent by mutableStateOf(false)
        internal set

    // 事件回调
    internal var _onBackClick: (() -> Unit)? = null
    internal var _onMenuClick: (() -> Unit)? = null
    internal var _onMenuItemClick: ((MenuItemData) -> Unit)? = null
    internal var _onPointerClick: (() -> Unit)? = null
    internal var _onExchangeClick: (() -> Unit)? = null

    // 菜单是否展开（用于竖屏模式）
    var menuExpanded by mutableStateOf(false)
        internal set

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

    // 显示/隐藏菜单
    fun showMenu() {
        menuExpanded = true
    }

    fun hideMenu() {
        menuExpanded = false
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
        themeColor = Color.Unspecified
        backgroundColor = Color.Unspecified
        orientation = AppBarOrientation.Vertical
        enableSubContent = false
        menuExpanded = false
    }
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
val LocalAppBarState: CompositionLocal<AppBarState?> = compositionLocalOf { null }

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

    fun themeColor(color: Color) {
        state.themeColor = color
    }

    fun backgroundColor(color: Color) {
        state.backgroundColor = color
    }

    fun orientation(orientation: AppBarOrientation) {
        state.orientation = orientation
    }

    fun enableSubContent(enable: Boolean) {
        state.enableSubContent = enable
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
