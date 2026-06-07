package cn.a10miaomiao.bilimiao.compose.common.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger

/**
 * 平台相关的 deep link 导航实现
 */
internal expect fun navigateDeepLink(navHostController: NavHostController, deepLink: String): Boolean

class PageNavigation(
    private val navHostController: () -> NavHostController,
    private val launchUrl: (url: String) -> Unit,
    private val scannerLauncher: (callback: (result: String) -> Unit) -> Boolean = { false },
    private val onClose: () -> Unit = {},
) : PageNavigator {

    val hostController get() = navHostController()

    private fun navigateByUriInternal(deepLink: String): Boolean {
        return navigateDeepLink(hostController, deepLink).also {
            if (!it) miaoLogger() debug "[NotFoundPage]:deepLink=$deepLink"
        }
    }

    override fun <T : ComposePage> navigate(route: T) {
        navigate(route, null, null)
    }

    fun <T : ComposePage> navigate(
        route: T,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null
    ) {
        hostController.navigate(route, navOptions, navigatorExtras)
    }

    fun <T : ComposePage> navigate(
        route: T,
        builder: NavOptionsBuilder.() -> Unit
    ) {
        navigate(route, navOptions(builder))
    }

    override fun canPopBackStack(): Boolean {
        return hostController.previousBackStackEntry != null
    }

    override fun popBackStack(): Boolean {
        return hostController.popBackStack().also {
            if (!it) {
                onClose()
            }
        }
    }

    override fun <T : ComposePage> popBackStack(
        route: T,
        inclusive: Boolean,
        saveState: Boolean
    ) {
        if(!hostController.popBackStack(route, inclusive, saveState)) {
            onClose()
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
