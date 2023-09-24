package com.a10miaomiao.bilimiao.comm.mypage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao

class MyPageConfig(
    private val fragment: Fragment,
    private val getConfigInfo: (() -> MyPageConfigInfo),
): DefaultLifecycleObserver {

    var setConfig: ((MyPageConfigInfo) -> Unit)? = null

    private val configInfo get() = getConfigInfo()

    init {
        fragment.lifecycle.addObserver(this)
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (setConfig == null) {
            // 无奈之取，activity重启后fragment自动恢复后不会经过onAttachFragment方法，setConfig为空
            val fragmentManager = fragment.parentFragmentManager
            val clazz = FragmentManager::class.java
            val method = clazz.getDeclaredMethod("dispatchOnAttachFragment", Fragment::class.java)
            method.isAccessible = true
            method.invoke(fragmentManager, fragment)
        }
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
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