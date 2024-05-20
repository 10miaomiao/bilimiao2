package com.a10miaomiao.bilimiao.comm.navigation

import android.app.Activity
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import com.a10miaomiao.bilimiao.MainActivity
import com.a10miaomiao.bilimiao.activity.SearchActivity

fun NavController.tryPopBackStack(): Boolean {
    return try {
        popBackStack()
        true
    } catch (e: Exception) {
        false
    }
}

fun NavController.navigateToCompose(
    url: String,
    navOptions: NavOptions? = null,
) {
    val curFragment = findPrimaryNavigationFragment()
    if (curFragment is ComposeFragment) {
        curFragment.composeNav
            .stopSameUrlCompose(url)
            ?.navigate(url)
        return
    }
    navigate(
        ComposeFragmentNavigatorBuilder.actionId,
        ComposeFragmentNavigatorBuilder.createArguments(url),
        navOptions,
    )
}

fun NavController.navigateToCompose(
    page: ComposePage,
    navOptions: NavOptions? = null,
) = navigateToCompose(page.url(), navOptions,)

inline fun <T : ComposePage> NavController.navigateToCompose(
    page: T,
    navOptions: NavOptions? = null,
    initArgs: T.() -> Unit,
) = navigateToCompose(page.also(initArgs), navOptions)

fun Activity.openSearch(view: View) {
    val searchConfig = (this as? MainActivity)?.pageConfig?.search
    if (searchConfig != null) {
        SearchActivity.launch(
            this,
            searchConfig.keyword,
            1,
            searchConfig.name,
            view,
        )
    } else {
        SearchActivity.launch(
            this,
            "",
            0,
            null,
            view,
        )
    }
}

//fun ScaffoldView.openSearchDrawer() {
//    openDrawer()
//    (drawerFragment as? StartFragment)?.openSearchView()
//    val intent = Intent(context, SearchActivity::class.java)
//    context.startActivity(intent)
//}
//
//fun ScaffoldView.closeSearchDrawer() {
//    closeDrawer()
//}