package com.a10miaomiao.bilimiao.comm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment

interface NavHosts {
    val pointerNavHostFragment:NavHostFragment
    val currentNavHostFragment:NavHostFragment
    val anotherNavHostFragment:NavHostFragment

    val pointerNavController get() = pointerNavHostFragment.navController
    val currentNavController get() = currentNavHostFragment.navController
    val anotherNavController get() = anotherNavHostFragment.navController

    fun NavController.navigateCompose(
        url: String,
        navOptions: NavOptions? = null,
    )
}