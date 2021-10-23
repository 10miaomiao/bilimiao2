package cn.a10miaomiao.miao.binding

class MiaoBinding {
    @PublishedApi internal var counter = 0

    class BindingData(
        val target: Any,
        var state: Any?
    )

    private val bindingList = arrayListOf<BindingData>()

    @PublishedApi internal fun <V> next (value: Any?, view: V): V? {
        val curCounter = counter
        counter++
        return if (bindingList.size > curCounter) {
            if (bindingList[curCounter].state == value) {
                null
            } else {
                bindingList[curCounter].state = value
                bindingList[curCounter].target as V
            }
        } else {
            bindingList.add(
                BindingData(
                    view as Any,
                    value
                )
            )
            view
        }
    }

    @PublishedApi internal fun <V> next (value: Any?, initialTarget: () -> V): V? {
        val curCounter = counter
        counter++
        return if (bindingList.size > curCounter) {
            if (bindingList[curCounter].state == value) {
                null
            } else {
                bindingList[curCounter].state = value
                bindingList[curCounter].target as V
            }
        } else {
            val target = initialTarget()
            bindingList.add(
                BindingData(
                    target as Any,
                    value
                )
            )
            target
        }
    }

//    @PublishedApi internal fun <V> next (initial: () -> V): V? {
//        val curCounter = counter
//        counter++
//        return if (bindingList.size > curCounter) {
//            bindingList[curCounter].target as V
//        } else {
//            val v = initial.invoke()
//            bindingList.add(
//                BindingData(
//                    v as Any,
//                    null
//                )
//            )
//            v
//        }
//    }

    fun cur (): BindingData {
        val curCounter = counter - 1
        return bindingList[curCounter]
    }

//    fun <T, V> setCur(state: T, target: V) {
//        val curCounter = counter - 1
//        bindingList[curCounter] = BindingData(
//            target,
//            state
//        )
//    }

//    inline fun <V> V.bind(binding: MiaoTarget<V>.() -> Unit): V {
//        MiaoTarget(this, this@MiaoBinding).binding()
//        return this
//    }

    inline fun <T> start (type: Int, block: () -> T): T {
        if (type == INIT) {
            clearBindingList()
        }
        counter = 0
        Bind.binding = this
        val result = block()
        Bind.binding = null
        return result
    }

    @PublishedApi internal fun clearBindingList () {
        bindingList.clear()
    }


    companion object {
        const val INIT = 0
        const val UPDATE = 1
    }
}