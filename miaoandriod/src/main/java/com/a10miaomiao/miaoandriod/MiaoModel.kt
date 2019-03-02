package com.a10miaomiao.miaoandriod

import com.a10miaomiao.miaoandriod.binding.MiaoBinding
import com.a10miaomiao.miaoandriod.binding.MiaoBindingImpl

open class MiaoModel(val binding: MiaoBinding) {

    fun setValue(value: MiaoModel){
        (binding as? MiaoBindingImpl)?.updateView()
    }
}