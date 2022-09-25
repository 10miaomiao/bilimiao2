package com.a10miaomiao.bilimiao.comm.mypage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class MyPageConfig(
    private val fragment: Fragment,
    private val getConfigInfo: (() -> MyPageConfigInfo),
): LifecycleObserver {

    var setConfig: ((MyPageConfigInfo) -> Unit)? = null

    private val configInfo get() = getConfigInfo()

    init {
        fragment.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if (setConfig == null) {
            // 无奈之取，activity重启后fragment自动恢复后不会经过onAttachFragment方法，setConfig为空
            val fragmentManager = fragment.parentFragment?.fragmentManager
            val clazz = FragmentManager::class.java
            val method = clazz.getDeclaredMethod("dispatchOnAttachFragment", Fragment::class.java)
            method.isAccessible = true
            method.invoke(fragmentManager, fragment)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        notifyConfigChanged()
    }

    fun notifyConfigChanged () {
        if (fragment.lifecycle.currentState == Lifecycle.State.RESUMED) {
            setConfig?.let {
                it(configInfo)
            }
        }
    }
}