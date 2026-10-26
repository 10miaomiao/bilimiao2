package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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

    // 底栏收缩进度：0 = 完全展开，1 = 完全收起（菜单行滑出，仅保留标题条）
    var collapseProgress by mutableFloatStateOf(0f)
        internal set

    val isCollapsed: Boolean
        get() = collapseProgress >= 1f

    // 吸附动画版本号：滚动等外部写入会自增，用于打断进行中的吸附动画
    private var collapseAnimationToken = 0

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

    /**
     * 由页面滚动驱动收缩，参照老项目的 quick-return：内容向上滚动即收起，向下滚动即展开。
     *
     * @param scrollDeltaPx 内容竖向滚动增量，正数表示内容向上滚动（AppBar 收起）
     * @param rangePx 从完全展开到完全收起的位移量（px），通常是菜单行高度
     */
    internal fun scrollCollapse(scrollDeltaPx: Float, rangePx: Float) {
        if (rangePx <= 0f || scrollDeltaPx == 0f) {
            return
        }
        val next = (collapseProgress + scrollDeltaPx / rangePx).coerceIn(0f, 1f)
        if (next == collapseProgress) {
            return
        }
        // 手指继续滑动时打断进行中的吸附动画
        collapseAnimationToken++
        collapseProgress = next
        if (scrollDeltaPx > 0f) {
            // 收起过程中菜单弹层会脱离锚点，直接关掉
            closeRootMenu()
        }
    }

    /**
     * 滚动（含惯性）结束后把收缩进度吸附到最近的一端。
     *
     * @param velocityY 剩余滚动速度（px/s），正数表示内容继续向下滚动（展开）
     * @param rangePx 从完全展开到完全收起的位移量（px）
     */
    internal suspend fun settleCollapse(velocityY: Float, rangePx: Float) {
        if (rangePx <= 0f) {
            return
        }
        val target = when {
            velocityY <= -CollapseFlingVelocity -> 1f
            velocityY >= CollapseFlingVelocity -> 0f
            collapseProgress >= 0.5f -> 1f
            else -> 0f
        }
        animateCollapseTo(target)
    }

    /** 立即展开（切页、横竖屏切换等场景，不做动画） */
    fun expandAppBar() {
        collapseAnimationToken++
        collapseProgress = 0f
    }

    /** 动画展开（点击收起后的标题条时触发） */
    internal suspend fun expandCollapsedAppBar() {
        animateCollapseTo(0f)
    }

    private suspend fun animateCollapseTo(target: Float) {
        if (collapseProgress == target) {
            return
        }
        val token = ++collapseAnimationToken
        animate(
            initialValue = collapseProgress,
            targetValue = target,
            animationSpec = tween(
                durationMillis = if (target > collapseProgress) {
                    CollapseAnimationDuration
                } else {
                    ExpandAnimationDuration
                },
                easing = FastOutSlowInEasing,
            ),
        ) { value, _ ->
            if (token == collapseAnimationToken) {
                collapseProgress = value
            }
        }
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
        collapseProgress = 0f
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

/** 触发吸附的剩余滚动速度阈值（px/s） */
private const val CollapseFlingVelocity = 600f

/** 收起动画时长（ms） */
private const val CollapseAnimationDuration = 180

/** 展开动画时长（ms） */
private const val ExpandAnimationDuration = 220

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
