package com.a10miaomiao.bilimiao.comm.navigation

import android.app.Activity
import android.view.View
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import com.a10miaomiao.bilimiao.MainActivity

fun Activity.openBottomSheet(page: ComposePage) {
    (this as? MainActivity)?.openBottomSheet(page)
}

fun Activity.openSearch(view: View? = null) {
    val activity = this as? MainActivity ?: return
    val searchConfig = activity.pageConfig?.search
    if (searchConfig != null) {
        activity.openSearchDialog(
            initKeyword = searchConfig.keyword,
            mode = 1,
            name = searchConfig.name,
        )
    } else {
        activity.openSearchDialog(
            initKeyword = "",
            mode = 0,
            name = null,
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
