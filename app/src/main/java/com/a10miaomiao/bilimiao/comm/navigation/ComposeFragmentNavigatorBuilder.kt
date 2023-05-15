package com.a10miaomiao.bilimiao.comm.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo

object ComposeFragmentNavigatorBuilder: FragmentNavigatorBuilder() {
    override val name: String = "compose"

    override fun FragmentNavigatorDestinationBuilder.init() {
        argument(MainNavArgs.url) {
            type = NavType.StringType
            nullable = true
        }
    }

    fun createArguments(
        url: String
    ): Bundle {
        return bundleOf(
            MainNavArgs.url to url
        )
    }
}