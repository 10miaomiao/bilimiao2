package cn.a10miaomiao.miao.binding

class MiaoTarget<V> (
    val target: V,
    val binding: MiaoBinding
) {

    inline fun <T> use(value: T, viewBinding: V.(T) -> Unit) {
        val realTarget = binding.next(value, target)
        if (realTarget != null) {
            viewBinding(realTarget, value)
        }
    }

}