package cn.a10miaomiao.miao.binding

class MiaoBinding {
    @PublishedApi internal var counter = 0

    class BindingData(
        val target: Any,
        var state: Any?,
    )

    class RefData<T>(
        var value: T
    )

    @PublishedApi internal var renderType = INIT
    private val bindingList = mutableListOf<BindingData>()
    private var persist = false

    private inline fun <T> _next (value: Any?, initialBindingData: () -> BindingData): T? {
        val curCounter = counter
        counter++
        return if (bindingList.size > curCounter) {
            if (persist) {
                persist = false
                if (eqValue(bindingList[curCounter].state, value)) {
                    null
                } else {
                    bindingList[curCounter].state = value
                    bindingList[curCounter].target as T
                }
            } else if (renderType == INIT) {
                val bindingData = initialBindingData()
                bindingList[curCounter] = bindingData
                bindingData.target as T
            } else if (eqValue(bindingList[curCounter].state, value)) {
                null
            } else {
                bindingList[curCounter].state = value
                bindingList[curCounter].target as T
            }
        } else {
            val bindingData = initialBindingData()
            bindingList.add(bindingData)
            bindingData.target as T
        }
    }

    private fun <T> eqValue(oldValue: T?, newValue: T?): Boolean {
        if (oldValue is List<*> && newValue is List<*>) {
            if (oldValue.size != newValue.size) {
                return false
            }
            for (i in 0 until oldValue.size) {
                if (oldValue[i] != newValue[i]) {
                    return false
                }
            }
            return true
        } else if (oldValue is Array<*> && newValue is Array<*>) {
            return oldValue.contentEquals(newValue)
        }
        return oldValue == newValue
    }

    @PublishedApi internal fun <T> next (value: Any?, target: T): T? {
        return _next(value) {
            BindingData(
                target as Any,
                value
            )
        }
    }

    @PublishedApi internal fun <V> next (value: Any?, initialTarget: () -> V): V? {
        return _next(value) {
            val target = initialTarget()
            BindingData(
                target as Any,
                value
            )
        }
    }

    fun cur (): BindingData {
        val curCounter = counter - 1
        return bindingList[curCounter]
    }

    fun persist () {
        this.persist = true
    }

    inline fun <T> start (type: Int, block: () -> T): T {
        renderType = type
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