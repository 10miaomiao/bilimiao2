package com.a10miaomiao.bilimiao.comm

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

object NavHosts {
    var getMainHostFrag:()->NavHostFragment?={ null } //主内容
    var getSubHostFrag:()->NavHostFragment?={ null }  //副内容
    var getCurrentHostFrag:()->NavHostFragment?={ null } //焦点所在处
    var getAnotherHostFrag:()->NavHostFragment?={ null } //非焦点处
    var getPointerHostFrag:()->NavHostFragment?={ null } //跟随指示器
    var getBottomSheetHostFrag:()->NavHostFragment?={ null } //底部弹出页面


    val mainNavController
        get() = getMainHostFrag()!!.navController
    val subNavController
        get() = getSubHostFrag()!!.navController
    val currentNavController
        get() = getCurrentHostFrag()!!.navController
    val anotherNavController
        get() = getAnotherHostFrag()!!.navController
    val pointerNavController
        get() = getPointerHostFrag()!!.navController
    val bottomSheetNavController
        get() = getBottomSheetHostFrag()!!.navController

    fun getNavHostFragment(controller:NavController):NavHostFragment?{
        return if(controller === mainNavController){
            getMainHostFrag()
        } else if (controller === subNavController){
            getSubHostFrag()
        } else if (controller === bottomSheetNavController){
            getBottomSheetHostFrag()
        } else {
            null
        }
    }
}