package com.a10miaomiao.miaoandriod.adapter

class MiaoList<E> : ArrayList<E>(){

    var updateView: (() -> Unit)? = null

    override fun add(element: E): Boolean {
        var r = super.add(element)
        updateView?.invoke()
        return r
    }

    override fun add(index: Int, element: E) {
        super.add(index, element)
        updateView?.invoke()
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var r = super.addAll(elements)
        updateView?.invoke()
        return r
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean {
        var r = super.addAll(index, elements)
        updateView?.invoke()
        return r
    }

    override fun set(index: Int, element: E): E {
        super.set(index, element)
        updateView?.invoke()
        return element
    }

    override fun remove(element: E): Boolean {
        var r =  super.remove(element)
        updateView?.invoke()
        return r
    }
}
