package com.a10miaomiao.miaoandriod

import android.arch.lifecycle.*
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import java.util.ArrayList

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

    fun v(): LifecycleOwner.() -> ValueManager<T> = {
        { f ->
            if (lifecycle.currentState != Lifecycle.State.DESTROYED)
                f(_value)
            observe(this, Observer { newValue ->
                f(_value)
            })
        }
    }

    fun <R> v(getValue: T.(T) -> R): LifecycleOwner.() -> ValueManager<R> = {
        { f ->
            if (lifecycle.currentState != Lifecycle.State.DESTROYED)
                f(getValue(_value, _value))
            observe(this, Observer { newValue ->
                f(getValue(_value, _value))
            })
        }
    }

    fun <R> miaoList(getList: T.(T) -> List<R>?): LifecycleOwner.() -> MiaoList<R> = {
        val list = MiaoList<R>()
        list.addAll(getList(_value, _value) ?: ArrayList())
        observe(this, Observer {
            list.clear()
            list.addAll(getList(_value, _value) ?: ArrayList())
        })
        list
    }


}

fun mergeMiaoObserver(vararg observers: MiaoObserver<Any>): MiaoObserverAll {
    return { observer ->
        observers.forEach { it { observer() } }
    }
}