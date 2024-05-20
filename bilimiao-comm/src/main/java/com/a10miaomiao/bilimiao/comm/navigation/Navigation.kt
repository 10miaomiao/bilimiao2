package com.a10miaomiao.bilimiao.comm.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment


/**
 * 在接口NavHosts中寻找
 */
fun NavController.findNavHostFragment(): NavHostFragment?{
    val navHosts = (context as? NavHosts) ?: return null
    return if(this === navHosts.currentNavController){
        navHosts.currentNavHostFragment
    } else if(this === navHosts.anotherNavController){
        navHosts.anotherNavHostFragment
    } else {
        null
    }
}

fun NavController.inNavHosts(): Boolean{
    return (context as? NavHosts)?.let{
        this === it.currentNavController || this === it.anotherNavController
    } ?: false
}

/**
 * 在接口NavHosts中寻找
 */
fun NavController.findPrimaryNavigationFragment(): Fragment?{
    return this.findNavHostFragment()?.childFragmentManager?.primaryNavigationFragment
}

fun NavController.currentUrl(): String?{
    val arguments = this.currentBackStackEntry?.arguments
    val intent = arguments?.getParcelable<Intent>(NavController.KEY_DEEP_LINK_INTENT)
    return intent?.data?.toString()
}
fun NavController.currentId(): Int?{
    val id = this.currentBackStackEntry?.destination?.id
    return id
}
fun NavController.currentArgs(): Bundle?{
    val arguments = this.currentBackStackEntry?.arguments
    return arguments
}


/**
 * 参数url与顶部页面url相同时返回null
 * 否则返回自身
 */
@SuppressLint("RestrictedApi")
fun NavController.stopSameUrl(url:String): NavController?{
    return if(NavDestination.createRoute(url) == currentUrl()) {
        null
    } else {
        this
    }
}

/**
 * 寻找指示器处的controller，未找到则返回自身
 */
fun NavController.pointerOrSelf(): NavController{
    val navHosts = (context as? NavHosts) ?: return this
    return navHosts.pointerNavController
}

/**
 * 寻找焦点所在controller，未找到则返回自身
 */
fun NavController.currentOrSelf(): NavController{
    val navHosts = (context as? NavHosts) ?: return this
    return navHosts.currentNavController
}

/**
 * 寻找焦点另一侧的controller，未找到则返回自身
 */
fun NavController.anotherOrSelf(): NavController{
    val navHosts = (context as? NavHosts) ?: return this
    return navHosts.anotherNavController
}