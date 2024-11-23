package cn.a10miaomiao.bilimiao.compose.common.navigation

import android.app.Activity
import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.Navigation
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute

object BottomSheetNavigation {

    private fun getNavBottomSheetFragmentID(
        context: Context,
    ): Int {
        return context.resources.getIdentifier(
            "nav_bottom_sheet_fragment",
            "id",
            context.packageName,
        )
    }

    fun getNavController(activity: Activity): NavController {
        return Navigation.findNavController(activity, getNavBottomSheetFragmentID(activity))
    }

    fun navigate(activity: Activity, url: String) {
        val arguments = ComposeFragment.createArguments(url)
        getNavController(activity).navigate(ComposeFragment.actionId, arguments)
    }

    fun navigate(activity: Activity, entry: BilimiaoPageRoute.Entry, param: String) {
        val arguments = ComposeFragment.createArguments(entry, param)
        getNavController(activity).navigate(ComposeFragment.actionId, arguments)
    }

}