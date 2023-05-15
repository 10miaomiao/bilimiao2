package com.a10miaomiao.bilimiao.comm.navigation

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder

abstract class FragmentNavigatorBuilder {

    private var _id: Int = 0
    private var _actionId: Int = 0

    val id: Int get() = _id
    val actionId: Int get() = _actionId

    abstract val name: String
    open fun FragmentNavigatorDestinationBuilder.init() {}

    fun FragmentNavigatorDestinationBuilder.build(id: Int, actionId: Int) {
        _id = id
        _actionId = actionId
        init()
    }
}