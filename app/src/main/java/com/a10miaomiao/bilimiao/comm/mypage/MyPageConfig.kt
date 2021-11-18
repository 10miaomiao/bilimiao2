package com.a10miaomiao.bilimiao.comm.mypage

import androidx.lifecycle.Lifecycle

class MyPageConfig(
    private val lifecycle: Lifecycle,
    private val getConfigInfo: (() -> MyPageConfigInfo),
) {
    var setConfig: ((MyPageConfigInfo) -> Unit)? = null

    private val configInfo get() = getConfigInfo()

    fun notifyConfigChanged () {
        if (
            lifecycle.currentState == Lifecycle.State.DESTROYED
            || lifecycle.currentState == Lifecycle.State.CREATED
        ) {
            return
        }
        setConfig?.let {
            it(configInfo)
        }
    }
}