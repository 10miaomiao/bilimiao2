package com.a10miaomiao.miaoandriod

open class MiaoModel(val binding: MiaoBinding) {

    fun setValue(value: MiaoModel){
        (binding as? MiaoBindingImpl)?.updateView()
    }
}