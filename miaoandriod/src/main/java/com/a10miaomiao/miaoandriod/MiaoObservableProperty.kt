package com.a10miaomiao.miaoandriod

import com.a10miaomiao.miaoandriod.binding.MiaoBinding
import com.a10miaomiao.miaoandriod.binding.key
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MiaoObservableProperty<T>(val binding: MiaoBinding?, initialValue: T) : ReadWriteProperty<Any?, T>{

    var value = initialValue

    override fun getValue(thisRef: Any?, property: KProperty<*>): T  = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val oldValue = this.value
        if (beforeChange(property, oldValue, value)){
            this.value = value
            afterChange(property, oldValue, value)
        }
    }
    fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean {
        return oldValue != newValue
    }

    fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        binding?.updateView(property.key)
    }
}