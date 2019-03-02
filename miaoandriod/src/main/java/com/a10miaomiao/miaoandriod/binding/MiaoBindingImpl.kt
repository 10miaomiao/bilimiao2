package com.a10miaomiao.miaoandriod.binding

open class MiaoBindingImpl : MiaoBinding {
    var bindFns: Map<String, List<() -> Unit>> = mapOf()

    override fun bindData(fn: () -> Unit, key: String) {
        var mfuns = bindFns.toMutableMap()
        if (bindFns.containsKey(key)) {
            var fs = bindFns[key]!!.toMutableList()
            fs.add(fn)
            mfuns[key] = fs
        } else {
            mfuns[key] = arrayListOf(fn)
        }
        bindFns = mfuns
    }

    override fun updateView(key: String) {
        bindFns[key]?.forEach { it() }
        bindFns["all"]?.forEach { it() }
    }

    fun updateView() {
        bindFns.forEach { it.value.forEach { it() }}
    }
}