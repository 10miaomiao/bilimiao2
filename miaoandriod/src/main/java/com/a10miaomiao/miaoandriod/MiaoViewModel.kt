package com.a10miaomiao.miaoandriod

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MiaoViewModel : ViewModel() {

    val hookList = arrayListOf<Hook>()

    fun <T> useObservable(getter: ValueGetter<T>): ObservablerDispatch<T> {
        val liveData = MutableLiveData<T>()
        val value = getter.getValue()
        liveData.value = value
        hookList.add(Hook(
                getter as ValueGetter<Any>,
                value as Any,
                liveData as MutableLiveData<Any>
        ))
        return ObservablerDispatch(
                value as T,
                { observer ->
                    liveData.observe(this, android.arch.lifecycle.Observer { newValue ->
                        newValue?.let { observer.onChanged(it as T) }
                    })
                }
        )
    }

    fun setData(fn: () -> Unit) = doAsync {
        fn()
        uiThread {
            forceUpdate()
        }
    }

    fun forceUpdate() {
        hookList.forEachIndexed { index, hook ->
            val newValue = hook.valueGetter.getValue()
            if (hook.value != newValue) {
                // 数据改动，更新
                hook.value = newValue
                hook.liveData.value = newValue
            }
        }
    }

    interface ValueGetter<T> {
        fun getValue(): T
    }

    interface Observer<T> {
        fun onChanged(t: T)
    }

    data class Hook(
            val valueGetter: ValueGetter<Any>,
            var value: Any,
            val liveData: MutableLiveData<Any>
    )

    data class ObservablerDispatch<T>(
            val value: T,
            val observabler: LifecycleOwner.(observer: Observer<T>) -> Unit
    )

}