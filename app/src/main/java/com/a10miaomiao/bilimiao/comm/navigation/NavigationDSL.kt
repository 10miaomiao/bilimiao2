package com.a10miaomiao.bilimiao.comm.navigation

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.navigation.NavController
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import com.a10miaomiao.bilimiao.MainActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.activity.SearchActivity
import com.a10miaomiao.bilimiao.comm.mypage.SearchConfigInfo
import com.a10miaomiao.bilimiao.page.search.SearchStartFragment
import com.a10miaomiao.bilimiao.page.start.StartFragment
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView
import java.lang.Exception

fun NavController.tryPopBackStack(): Boolean {
    return try {
        popBackStack()
        true
    } catch (e: Exception) {
        false
    }
}

fun NavController.navigateToCompose(url: String) = navigate(
    ComposeFragmentNavigatorBuilder.actionId,
    ComposeFragmentNavigatorBuilder.createArguments(url)
)

fun NavController.navigateToCompose(page: ComposePage) = navigate(
    ComposeFragmentNavigatorBuilder.actionId,
    ComposeFragmentNavigatorBuilder.createArguments(page.url())
)

inline fun <T : ComposePage> NavController.navigateToCompose(
    page: T,
    initArgs: T.() -> Unit,
) = navigateToCompose(page.also(initArgs))

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