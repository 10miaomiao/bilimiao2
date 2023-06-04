package com.a10miaomiao.bilimiao.comm.navigation

import android.app.Activity
import androidx.navigation.NavController
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.page.search.SearchStartFragment
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.comm.getScaffoldView

fun NavController.navigateToCompose(url: String) = navigate(
    ComposeFragmentNavigatorBuilder.actionId,
    ComposeFragmentNavigatorBuilder.createArguments(url)
)



fun ScaffoldView.openSearchDrawer() {
    openDrawer()
    (drawerFragment as? SearchStartFragment)?.showSoftInput()
}

fun ScaffoldView.closeSearchDrawer() {
    (drawerFragment as? SearchStartFragment)?.hideSoftInput()
    closeDrawer()
}