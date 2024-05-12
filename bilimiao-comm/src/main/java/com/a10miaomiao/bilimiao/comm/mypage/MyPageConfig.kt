package com.a10miaomiao.bilimiao.comm.mypage

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*

class MyPageConfig(
    private val fragment: Fragment,
    private val getConfigInfo: (() -> MyPageConfigInfo),
) {

    var setConfig: (() -> Unit)? = null

    val configInfo get() = getConfigInfo()

    private val lifecycleObserver = object : DefaultLifecycleObserver {
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

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            notifyConfigChanged()
            fragment.view?.announceForAccessibility(configInfo.title)
        }
    }

    init {
        fragment.lifecycle.addObserver(lifecycleObserver)
    }

    fun notifyConfigChanged () {
        if (fragment.lifecycle.currentState == Lifecycle.State.RESUMED) {
            setConfig?.let {
                it()
            }
        }
    }
}