package com.a10miaomiao.bilimiao.comm.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import com.a10miaomiao.bilimiao.MainActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.activity.SearchActivity
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.page.start.StartFragment
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.a10miaomiao.bilimiao.widget.scaffold.getScaffoldView

fun NavController.tryPopBackStack(): Boolean {
    return try {
        popBackStack()
        true
    } catch (e: Exception) {
        false
    }
}

@SuppressLint("RestrictedApi")
fun NavController.navigateToCompose(
    entry: BilimiaoPageRoute.Entry,
    param: String = "",
    navOptions: NavOptions? = null,
) {
    val curFragment = (context as? MainActivity)
        ?.getPrimaryNavigationFragment(this)
    if (curFragment is ComposeFragment) {
        val composeNav = curFragment.composeNav
        val composePage = BilimiaoPageRoute.getEntryRoute(entry, param)
//        val arguments = composeNav.currentBackStackEntry?.arguments
//        val intent = arguments?.getParcelable<Intent>(NavController.KEY_DEEP_LINK_INTENT)
//        val curUrl = intent?.data?.toString()
//        if (curUrl != null && curUrl == NavDestination.createRoute(url)) {
//            return
//        }
        composeNav.navigate(composePage)
        return
    }
    navigate(
        ComposeFragmentNavigatorBuilder.actionId,
        ComposeFragmentNavigatorBuilder.createArguments(entry, param),
        navOptions,
    )
}

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