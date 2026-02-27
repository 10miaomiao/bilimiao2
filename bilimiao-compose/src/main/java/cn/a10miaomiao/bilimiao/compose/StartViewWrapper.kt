package cn.a10miaomiao.bilimiao.compose

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod
import kotlin.math.max
import kotlin.math.min

class StartViewWrapper(
    val activity: Activity,
    val navigateTo: (ComposePage) -> Unit,
    val navigateUrl: (String) -> Unit,
    val dismissRequest: () -> Unit,
    val openScanner: (callback: (result: String) -> Unit) -> Boolean,
) {

    private val composeView = ComposeView(activity)

    private val density = activity.resources.displayMetrics.density

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

    var playerView: View? = null
    var completionView: View? = null
    var errorMessageView: View? = null
    var areaLimitView: View? = null
    var loadingView: View? = null

    private val _showPlayer = mutableStateOf(false)
    val showPlayer get() = _showPlayer.value

    private val _orientation = mutableStateOf(1)
    val orientation get() = _orientation.value

    private val _fullScreenPlayer = mutableStateOf(false)
    val fullScreenPlayer get() = _fullScreenPlayer.value

    private val _smallModePlayerMinHeight = mutableStateOf(0)
    val smallModePlayerMinHeight get() = _smallModePlayerMinHeight.value

    private val _smallModePlayerCurrentHeight = mutableStateOf(0)
    val smallModePlayerCurrentHeight get() = _smallModePlayerCurrentHeight.value

    private val _playerSmallShowAreaWidth = mutableStateOf(0)
    val playerSmallShowAreaWidth get() = _playerSmallShowAreaWidth.value

    private val _playerSmallShowAreaHeight = mutableStateOf(0)
    val playerSmallShowAreaHeight get() = _playerSmallShowAreaHeight.value

    var shouldCreateCompositionOnAttachedToWindow = true
        private set

    fun getView(): View {
        return composeView
    }

    fun setContent(
        parent: CompositionContext,
        content: @Composable () -> Unit
    ) {
        shouldCreateCompositionOnAttachedToWindow = false
        composeView.setParentCompositionContext(parent)
        composeView.setContent(content)
    }

    fun setTouchStartTop(topHeight: Float) {
        var topHeightDp = topHeight / density
        val windowHeightDp = getWindowHeight() / density
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
        dismissRequest()
    }

    fun setShowPlayer(value: Boolean) {
        _showPlayer.value = value
    }

    fun setOrientation(value: Int) {
        _orientation.value = value
    }

    fun setFullScreenPlayer(value: Boolean) {
        _fullScreenPlayer.value = value
    }

    fun setSmallModePlayerMinHeight(value: Int) {
        _smallModePlayerMinHeight.value = value
    }

    fun setSmallModePlayerCurrentHeight(value: Int) {
        _smallModePlayerCurrentHeight.value = value
    }

    fun setPlayerSmallShowArea(width: Int, height: Int) {
        _playerSmallShowAreaWidth.value = width
        _playerSmallShowAreaHeight.value = height
    }

    private fun getWindowHeight(): Int {
        return activity.window.decorView.height
    }

}
