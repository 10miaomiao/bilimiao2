package cn.a10miaomiao.bilimiao.compose.common.navigation

import android.net.Uri
import android.util.TypedValue
import androidx.annotation.MainThread
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger

class PageNavigation(
    private val navHostController: () -> NavHostController,
    private val launchUrl: (uri: Uri) -> Unit,
    private val onClose: () -> Unit = {},
) {

    val hostController get() = navHostController()

    fun navigateByUri(deepLink: Uri): Boolean {
        return runCatching {
            hostController.navigate(deepLink)
        }.isSuccess.also {
            if (!it) miaoLogger() debug "[NotFoundPage]:deepLink=${deepLink}"
        }
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

    fun popBackStack() {
        if (!hostController.popBackStack()) {
            onClose()
        }
    }

    fun <T : ComposePage> popBackStack(
        route: T,
        inclusive: Boolean,
        saveState: Boolean = false
    ) {
        if(!hostController.popBackStack(route, inclusive, saveState)) {
            onClose()
        }
    }

    fun navigateToVideoInfo(id: String) {
        hostController.navigate(VideoDetailPage(id))
    }

    fun launchWebBrowser(url: String) {
        launchWebBrowser(Uri.parse(url))
    }

    fun launchWebBrowser(uri: Uri) {
        launchUrl(uri)
    }

}