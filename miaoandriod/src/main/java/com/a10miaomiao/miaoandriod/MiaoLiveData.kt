package com.a10miaomiao.miaoandriod

import android.arch.lifecycle.*

typealias MiaoObserver<T> = LifecycleOwner.(observer: (t: T) -> Unit) -> Unit
typealias MiaoObserverAll = LifecycleOwner.(observer: () -> Unit) -> Unit

open class MiaoLiveData<T>(private var _value: T) : LiveData<T>() {

    inline infix fun set(value: T) = setValue(value)

    inline infix fun post(value: T) = postValue(value)

    override fun setValue(value: T) {
        _value = value
        super.setValue(value)
    }

    override fun postValue(value: T) {
        _value = value
        super.postValue(value)
    }

    override fun getValue(): T {
        return _value
    }

    open fun observe(): MiaoObserver<T> {
        return { observer ->
            if (lifecycle.currentState != Lifecycle.State.DESTROYED)
                observer(_value)
            observe(this, Observer { newValue ->
                observer(_value)
            })
        }
    }

    open fun observeNotNull(): MiaoObserver<T> {
        return { observer ->
            if (lifecycle.currentState != Lifecycle.State.DESTROYED)
                _value?.let { observer(it) }
            observe(this, Observer { newValue ->
                newValue?.let { observer(it) }
            })
        }
    }

    inline operator fun unaryPlus() = observe()
    operator fun unaryMinus() = _value
    operator fun invoke() = _value

}

fun mergeMiaoObserver(vararg observers: MiaoObserver<Any>): MiaoObserverAll {
    return { observer ->
        observers.forEach { it { observer() } }
    }
}