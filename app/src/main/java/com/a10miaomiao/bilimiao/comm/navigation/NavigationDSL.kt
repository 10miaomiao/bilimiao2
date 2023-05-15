package com.a10miaomiao.bilimiao.comm.navigation

import androidx.navigation.NavController
import cn.a10miaomiao.bilimiao.compose.ComposeFragment

fun NavController.navigateToCompose(url: String) = navigate(
    ComposeFragmentNavigatorBuilder.actionId,
    ComposeFragmentNavigatorBuilder.createArguments(url)
)