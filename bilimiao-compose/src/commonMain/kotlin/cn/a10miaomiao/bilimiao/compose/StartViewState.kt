package cn.a10miaomiao.bilimiao.compose

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.max
import kotlin.math.min

class StartViewState(
    fullScreenPlayer: StateFlow<Boolean> = MutableStateFlow(false),
    pictureInPicture: StateFlow<Boolean> = MutableStateFlow(false),
    scope: CoroutineScope,
) {

    val playerState = PlayerState(fullScreenPlayer, pictureInPicture, scope)

    /** 抽屉对外状态（唯一状态源），取值见 [DRAWER_STATE_COLLAPSED] 等常量 */
    private val _drawerState = mutableStateOf(DRAWER_STATE_COLLAPSED)
    val drawerState get() = _drawerState.value

    /**
     * 抽屉是否处于打开（含打开过程）状态
     *
     * 只要不是 [DRAWER_STATE_COLLAPSED] 就算打开：[DRAWER_STATE_DRAGGING] / [DRAWER_STATE_SETTLING]
     * 也是用户可见的打开过程，返回键应据此先关抽屉，而不是穿透到页面返回。
     */
    val drawerOpen get() = _drawerState.value != DRAWER_STATE_COLLAPSED

    private val _touchStart = mutableFloatStateOf(0f)
    val touchStart get() = _touchStart.floatValue
    private val _showSearchDialog = mutableStateOf(false)
    val showSearchDialog get() = _showSearchDialog.value

    private val _searchInitKeyword = mutableStateOf("")
    val searchInitKeyword get() = _searchInitKeyword.value
    private val _searchInitMode = mutableStateOf(0)
    val searchInitMode get() = _searchInitMode.value

    private val _pageSearchMethod = mutableStateOf<PageSearchMethod?>(null)
    val pageSearchMethod get() = _pageSearchMethod.value

    private val _searchAnimation = mutableStateOf(false)
    val searchAnimation get() = _searchAnimation.value

    fun setTouchStartTop(topHeightPx: Float, windowHeightPx: Int, density: Float) {
        var topHeightDp = topHeightPx / density
        val windowHeightDp = windowHeightPx / density
        topHeightDp = min(topHeightDp - 200, windowHeightDp - 400)
        topHeightDp = max(topHeightDp, 0f)
        _touchStart.value = topHeightDp
    }

    fun setPageSearchMethod(method: PageSearchMethod?) {
        _pageSearchMethod.value = method
    }

    fun openSearchDialog(keyword: String, mode: Int, animation: Boolean) {
        _searchAnimation.value = animation
        _searchInitKeyword.value = keyword
        _searchInitMode.value = mode
        _showSearchDialog.value = true
    }

    fun closeSearchDialog() {
        _searchAnimation.value = true
        _showSearchDialog.value = false
    }

    fun openDrawer() {
        _drawerState.value = DRAWER_STATE_EXPANDED
    }

    fun closeDrawer() {
        _drawerState.value = DRAWER_STATE_COLLAPSED
    }

    fun setDrawerState(state: Int) {
        _drawerState.value = state
    }

    fun isDrawerOpen(): Boolean {
        return drawerOpen
    }

    companion object {
        const val DRAWER_STATE_DRAGGING = 1
        const val DRAWER_STATE_SETTLING = 2
        const val DRAWER_STATE_EXPANDED = 3
        const val DRAWER_STATE_COLLAPSED = 4
    }

}
