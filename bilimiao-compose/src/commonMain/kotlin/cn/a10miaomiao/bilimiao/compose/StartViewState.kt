package cn.a10miaomiao.bilimiao.compose

import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod
import kotlin.math.max
import kotlin.math.min

class StartViewState(
    onFloatingPlayerLayoutStateChanged: (PlayerFloatingLayoutState) -> Unit = {},
) {

    val playerState = PlayerState(onFloatingPlayerLayoutStateChanged)

    private val _drawerState = mutableStateOf(DRAWER_STATE_COLLAPSED)
    val drawerState get() = _drawerState.value

    private val _drawerOpen = mutableStateOf(false)
    val drawerOpen get() = _drawerOpen.value

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
        _drawerOpen.value = true
        _drawerState.value = DRAWER_STATE_EXPANDED
    }

    fun closeDrawer() {
        _drawerOpen.value = false
        _drawerState.value = DRAWER_STATE_COLLAPSED
    }

    fun setDrawerState(state: Int) {
        _drawerState.value = state
        _drawerOpen.value = state != DRAWER_STATE_COLLAPSED
    }

    fun isDrawerOpen(): Boolean {
        return _drawerOpen.value
    }

    companion object {
        const val DRAWER_STATE_DRAGGING = 1
        const val DRAWER_STATE_SETTLING = 2
        const val DRAWER_STATE_EXPANDED = 3
        const val DRAWER_STATE_COLLAPSED = 4
    }

}
