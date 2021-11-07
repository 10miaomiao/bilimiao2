package cn.a10miaomiao.miao.binding

object Bind {
    var binding: MiaoBinding? = null
}

typealias ViewBindingFn<T, V> = V.(T) -> Unit

inline fun <T, V> miaoMemo(value: V, noinline initialMemo: (V) -> T): T {
    return Bind.binding!!.let {
//        it.persist()
        it.next(value) {
            initialMemo(value)
        } ?: it.cur().target as T
    }
}

inline fun <T> miaoRef(initialTarget: T): MiaoBinding.RefData<T> {
    return Bind.binding!!.let {
        it.persist()
        it.next(null, MiaoBinding.RefData(initialTarget))
        it.cur().target as MiaoBinding.RefData<T>
    }
}

inline fun <T, V> V.miaoEffect(value: T, viewBinding: ViewBindingFn<T, V>) {
    val realTarget = Bind.binding?.next(value, this)
    if (realTarget != null) {
        viewBinding(realTarget, value)
    }
}

inline fun <T, V> V.miaoEffect(value: T, viewBinding: ViewBindingFn<T, V>, viewUpdate: ViewBindingFn<T,V>) {
    val realTarget = Bind.binding?.next(value, this)
    if (realTarget != null) {
        viewBinding(realTarget, value)
    } else {
        Bind.binding?.cur()?.let {
            viewUpdate(it.target as V, value)
        }
    }
}