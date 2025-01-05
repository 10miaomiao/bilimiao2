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
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger

class PageNavigation(
    private val fragment: Fragment,
    private val navHostController: () -> NavHostController,
) {

    val hostController get() = navHostController()

    fun navigateByUri(deepLink: Uri): Boolean {
        return runCatching {
            hostController.navigate(deepLink)
        }.isSuccess || runCatching {
            val nav = fragment.findNavController()
            nav.navigate(deepLink, defaultNavOptions)
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
        hostController.popBackStack()
    }

    fun <T : ComposePage> popBackStack(
        route: T,
        inclusive: Boolean,
        saveState: Boolean = false
    ) {
        hostController.popBackStack(route, inclusive, saveState)
    }

    fun navigateToVideoInfo(id: String) {
        val nav = fragment.findNavController()
        nav.navigate(
            Uri.parse("bilimiao://video/${id}"),
            defaultNavOptions,
        )
    }

    fun launchWebBrowser(url: String) {
        launchWebBrowser(Uri.parse(url))
    }

    fun launchWebBrowser(uri: Uri) {
        // 使用外部浏览器打开
        val activity = fragment.requireActivity()
        val typedValue = TypedValue()
        val attrId = com.google.android.material.R.attr.colorSurfaceVariant
        activity.theme.resolveAttribute(attrId, typedValue, true)
        val intent = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(ContextCompat.getColor(activity, typedValue.resourceId))
                    .build()
            )
            .build()
        intent.launchUrl(activity, uri)
    }

}