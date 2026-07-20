package cn.a10miaomiao.bilimiao.compose.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries

import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * 底部 Tab 多 backstack 导航状态。
 *
 * 每个 top-level 路由（Tab）持有独立的 [NavBackStack]，切换 Tab 时只切换当前活跃栈，
 * 各 Tab 内部导航状态独立保留。参考 androidx nav3-recipes multiplestacks recipe。
 *
 * @param startRoute 启动 Tab，必须属于 [topLevelRoutes]
 * @param topLevelRoutes 所有 Tab 集合
 */
class BottomBarBackStack(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var topLevelRoute: NavKey by topLevelRoute
        internal set

    /**
     * 当前活跃 Tab 的 backstack（只读视图）。
     */
    val currentBackStack: NavBackStack<NavKey>
        get() = backStacks.getValue(topLevelRoute)

    /**
     * 当前活跃 Tab 的可变 backstack 引用（用于 add/removeLastOrNull）。
     */
    val current: NavBackStack<NavKey>
        get() = backStacks.getValue(topLevelRoute)

    /**
     * 是否还能 pop：当前栈长度 > 1，或还有其他 Tab 可回退。
     */
    fun canPop(): Boolean {
        val cur = current
        if (cur.size > 1) return true
        return topLevelRoute != startRoute
    }

    /**
     * 处理返回：当前 Tab 栈深度 > 1 时 pop 当前栈；否则回退到上一个 Tab（按访问顺序）。
     * 简化版：当前 Tab 为根且不是 startRoute 时，切回 startRoute。
     */
    fun pop(): Boolean {
        val cur = current
        return if (cur.size > 1) {
            cur.removeLastOrNull()
            true
        } else if (topLevelRoute != startRoute) {
            topLevelRoute = startRoute
            true
        } else {
            false
        }
    }

    /**
     * 导航到任意 key：若是 top-level 则切 Tab，否则 push 到当前栈。
     * 含 launchSingleTop 语义：目标已是栈顶则跳过。
     */
    fun navigate(key: NavKey) {
        // 用 key::class 比较，兼容 class（非 object）的 top-level 路由
        val topLevelKey = backStacks.keys.firstOrNull { it::class == key::class }
        if (topLevelKey != null) {
            topLevelRoute = topLevelKey
        } else {
            val cur = current
            if (cur.lastOrNull()?.takeIf { it::class == key::class } == null) {
                cur.add(key)
            }
        }
    }
}

/**
 * 记住 [BottomBarBackStack]，每个 top-level 路由拥有独立 [NavBackStack]。
 * 注意：此处用 remember 而非 rememberNavBackStack，暂不持久化到 SavedState；
 * 若需 saveable，每个 key 需加 @Serializable 并改用 rememberNavBackStack。
 */
@Composable
fun rememberBottomBarBackStack(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>,
): BottomBarBackStack {
    val topLevelRoute = remember(startRoute) { mutableStateOf(startRoute) }
    val backStacks = topLevelRoutes.associateWith { key ->
        remember(startRoute, key) { NavBackStack<NavKey>(key) }
    }
    return remember(startRoute, topLevelRoutes) {
        BottomBarBackStack(startRoute, topLevelRoute, backStacks)
    }
}

/**
 * 装饰所有在用 Tab 的 backstack 为 [NavEntry] 列表，供 [androidx.navigation3.ui.NavDisplay] 的 entries 重载使用。
 *
 * 返回 startRoute + 当前 Tab 的 entries 拼接（若不同），这样切换 Tab 时
 * 旧 Tab 的 NavEntry 仍在列表中，NavDisplay 保留其 composition 状态（ViewModel、rememberSaveable、滚动位置等）。
 * 参考官方 nav3-recipes multiplestacks 的 toDecoratedEntries。
 */
@Composable
fun BottomBarBackStack.decorateEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): List<NavEntry<NavKey>> {
    val decorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>())
    // 所有在用 Tab：startRoute 始终保留，加上当前 Tab（若不同）
    val inUseRoutes = if (topLevelRoute == startRoute) {
        listOf(startRoute)
    } else {
        listOf(startRoute, topLevelRoute)
    }
    return inUseRoutes.flatMap { route ->
        val stack = backStacks.getValue(route)
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }
}
