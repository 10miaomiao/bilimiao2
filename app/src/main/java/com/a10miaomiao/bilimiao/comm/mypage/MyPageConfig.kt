package com.a10miaomiao.bilimiao.comm.mypage

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao

class MyPageConfig(
    private val lifecycle: Lifecycle,
    private val getConfigInfo: (() -> MyPageConfigInfo),
): LifecycleObserver {

    var setConfig: ((MyPageConfigInfo) -> Unit)? = null

    private val configInfo get() = getConfigInfo()

    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        notifyConfigChanged()
    }

    fun notifyConfigChanged () {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            setConfig?.let {
                it(configInfo)
            }
        }
    }
}