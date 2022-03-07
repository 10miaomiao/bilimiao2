package cn.a10miaomiao.miao.binding.android.view

const val NO_GETTER = "Property does not have a getter"

inline val noGetter: Nothing
    get() = throw UnsupportedOperationException(NO_GETTER)