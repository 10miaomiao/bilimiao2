package com.a10miaomiao.bilimiao.comm.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.bilimiao.compose.BilimiaoPageRoute
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo

object ComposeFragmentNavigatorBuilder: FragmentNavigatorBuilder() {
    override val name: String = "compose"

    override fun FragmentNavigatorDestinationBuilder.init() {
        ComposeFragment.initFragmentNavigatorDestinationBuilder(
            this, id, actionId
        )
    }

    fun createArguments(
        url: String
    ): Bundle {
        return ComposeFragment.createArguments(url)
    }

    fun createArguments(
        entry: BilimiaoPageRoute.Entry,
        param: String,
    ): Bundle {
        return ComposeFragment.createArguments(entry, param)
    }
}