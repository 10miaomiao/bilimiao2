package com.a10miaomiao.bilimiao.comm.navigation

import android.app.Activity
import android.view.View
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import com.a10miaomiao.bilimiao.MainActivity
import com.a10miaomiao.bilimiao.activity.SearchActivity

fun Activity.openBottomSheet(page: ComposePage) {
    (this as? MainActivity)?.openBottomSheet(page)
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