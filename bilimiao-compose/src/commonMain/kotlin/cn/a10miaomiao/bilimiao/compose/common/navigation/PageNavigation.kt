package cn.a10miaomiao.bilimiao.compose.common.navigation

import androidx.navigation3.runtime.NavKey
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger

/**
 * 平台相关的 deep link 解析：把 URI 转成 [ComposePage]（NavKey）并加入 backstack。
 * 返回 false 表示 URI 无法解析。
 */
internal expect fun navigateDeepLink(backStack: BottomBarBackStack, deepLink: String): Boolean

class PageNavigation(
    private val bottomBar: BottomBarBackStack,
    private val launchUrl: (url: String) -> Unit,
    private val scannerLauncher: (callback: (result: String) -> Unit) -> Boolean = { false },
    private val onClose: () -> Unit = {},
) : PageNavigator {

    private fun navigateByUriInternal(deepLink: String): Boolean {
        return navigateDeepLink(bottomBar, deepLink).also {
            if (!it) miaoLogger() debug "[NotFoundPage]:deepLink=$deepLink"
        }
    }

    override fun <T : ComposePage> navigate(route: T) {
        bottomBar.navigate(route as NavKey)
    }

    override fun canPopBackStack(): Boolean {
        return bottomBar.canPop()
    }

    override fun popBackStack(): Boolean {
        return bottomBar.pop().also {
            if (!it) {
                onClose()
            }
        }
    }

    override fun navigateByUri(uriString: String): Boolean {
        return navigateByUriInternal(uriString)
    }

    override fun navigateToVideoInfo(id: String) {
        navigate(VideoDetailPage(
            id = id
        ))
    }

    override fun launchWebBrowser(url: String) {
        launchUrl(url)
    }

    override fun openScanner(callback: (result: String) -> Unit): Boolean {
        return scannerLauncher(callback)
    }

}
