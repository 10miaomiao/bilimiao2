package com.a10miaomiao.miaoandriod

import android.os.Bundle
import com.a10miaomiao.miaoandriod.binding.key
import kotlin.reflect.KMutableProperty0

class MiaoInstanceState {

    private val list_int = ArrayList<KMutableProperty0<Int>>(0)
    private val list_long = ArrayList<KMutableProperty0<Long>>(0)
    private val list_string = ArrayList<KMutableProperty0<String>>(0)
    private val list_float = ArrayList<KMutableProperty0<Float>>(0)
    private val list_double = ArrayList<KMutableProperty0<Double>>(0)

    fun addInt(v: KMutableProperty0<Int>) {
        list_int.add(v)
    }

    fun addLong(v: KMutableProperty0<Long>) {
        list_long.add(v)
    }

    fun addString(v: KMutableProperty0<String>) {
        list_string.add(v)
    }

    fun addFloat(v: KMutableProperty0<Float>) {
        list_float.add(v)
    }

    fun addDouble(v: KMutableProperty0<Double>) {
        list_double.add(v)
    }

    fun readInstanceState(state: Bundle) {
        list_int.forEach { if (state.containsKey(it.key)) it.set(state.getInt(it.key)) }
        list_long.forEach { if (state.containsKey(it.key)) it.set(state.getLong(it.key)) }
        list_string.forEach { if (state.containsKey(it.key)) it.set(state.getString(it.key)) }
        list_float.forEach { if (state.containsKey(it.key)) it.set(state.getFloat(it.key)) }
        list_double.forEach { if (state.containsKey(it.key)) it.set(state.getDouble(it.key)) }
    }

    fun saveInstanceState(state: Bundle) {
        list_int.forEach { state.putInt(it.key, it.get()) }
        list_long.forEach { state.putLong(it.key, it.get()) }
        list_string.forEach { state.putString(it.key, it.get()) }
        list_float.forEach { state.putFloat(it.key, it.get()) }
        list_double.forEach { state.putDouble(it.key, it.get()) }
    }
}