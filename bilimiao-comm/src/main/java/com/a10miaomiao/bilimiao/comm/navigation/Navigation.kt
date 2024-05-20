package com.a10miaomiao.bilimiao.comm.navigation

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
fun equalBundles(one: Bundle, two: Bundle): Boolean {
    if (one.size() != two.size())
        return false

    if (!one.keySet().containsAll(two.keySet()))
        return false

    for (key in one.keySet()) {
        val valueOne = one.get(key)
        val valueTwo = two.get(key)
        if (valueOne is Bundle && valueTwo is Bundle) {
            if (!equalBundles(valueOne , valueTwo)) return false
        } else if (valueOne != valueTwo) return false
    }

    return true
}

/**
 * compose页面使用
 * 参数url与顶部页面url相同时返回null
 * 否则返回自身
 */
@SuppressLint("RestrictedApi")
fun NavController.stopSameUrlCompose(url:String): NavController?{
    val curUrl = currentUrl() ?: return this
    return if(NavDestination.createRoute(url) == curUrl) {
        null
    } else {
        this
    }
}

/**
 * 非compose页面使用
 * 参数url与顶部页面url相同时返回null
 * 否则返回自身
 */
fun NavController.stopSameUrl(uri: Uri): NavController?{
    val url = uri.toString()
    val curUrl = currentUrl() ?: return this
    return if(url == curUrl) {
        null
    } else {
        this
    }
}
/**
 * 参数args与顶部页面args相同时返回null
 * 否则返回自身
 */
fun NavController.stopSameArgs(args:Bundle): NavController?{
    val curArgs = currentArgs() ?: return this
    return if(equalBundles(curArgs,args)) {
        null
    } else {
        this
    }
}

/**
 * 参数id与顶部页面id相同时返回null
 * 否则返回自身
 */
fun NavController.stopSameId(id:Int): NavController?{
    val curId = currentId() ?: return this
    return if(curId==id){
        null
    } else {
        this
    }
}

/**
 * 都相同时返回null
 * 否则返回自身
 */
 fun NavController.stopSameIdAndArgs(id:Int, args:Bundle): NavController?{
    val curArgs = currentArgs() ?: return this
    val curId = currentId() ?: return this
    return if(equalBundles(curArgs,args)&&curId==id) {
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