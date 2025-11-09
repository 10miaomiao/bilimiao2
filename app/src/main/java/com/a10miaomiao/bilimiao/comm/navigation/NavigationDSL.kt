package com.a10miaomiao.bilimiao.comm.navigation

import android.app.Activity
import android.view.View
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import com.a10miaomiao.bilimiao.MainActivity

fun Activity.openBottomSheet(page: ComposePage) {
    (this as? MainActivity)?.openBottomSheet(page)
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
