package cn.a10miaomiao.bilimiao.danmaku.model

class Duration(initialDuration: Long) : Cloneable {

    private var mInitialDuration: Long = initialDuration
    private var factor: Float = 1.0f

    @Volatile
    var value: Long = initialDuration
        private set

    fun setValue(initialDuration: Long) {
        mInitialDuration = initialDuration
        value = (mInitialDuration * factor).toLong()
    }

    fun setFactor(f: Float) {
        if (factor != f) {
            factor = f
            value = (mInitialDuration * f).toLong()
        }
    }

    public override fun clone(): Duration {
        return Duration(mInitialDuration).also {
            it.factor = this.factor
            it.value = this.value
        }
    }
}
