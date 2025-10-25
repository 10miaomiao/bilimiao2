package cn.a10miaomiao.bilimiao.compose

import android.app.Activity
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import kotlin.math.max
import kotlin.math.min

class StartViewWrapper(
    val activity: Activity,
    val navigateTo: (ComposePage) -> Unit,
    val navigateUrl: (String) -> Unit,
    val openSearch: () -> Unit,
    val openScanner: (callback: (result: String) -> Unit) -> Boolean,
) {

    private val composeView = ComposeView(activity)

    private val density = activity.resources.displayMetrics.density

    val touchStart = mutableFloatStateOf(0f)
    val showSearchDialog = mutableStateOf(false)
    val searchInitKeyword = mutableStateOf("")
    val searchInitMode = mutableStateOf(0)
    val searchSelfName = mutableStateOf<String?>(null)

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
        touchStart.value = topHeightDp
    }

    fun openSearchDialog(keyword: String, mode: Int, selfName: String?) {
        searchInitKeyword.value = keyword
        searchInitMode.value = mode
        searchSelfName.value = selfName
        showSearchDialog.value = true
    }

    fun closeSearchDialog() {
        showSearchDialog.value = false
    }

    private fun getWindowHeight(): Int {
        return activity.window.decorView.height
    }

}
