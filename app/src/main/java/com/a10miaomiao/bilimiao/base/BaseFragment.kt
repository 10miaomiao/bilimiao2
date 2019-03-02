package com.a10miaomiao.bilimiao.base

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.miaoandriod.anko.MiaoAnkoContext
import com.a10miaomiao.miaoandriod.anko.createMiaoAnkoContext
import com.a10miaomiao.miaoandriod.binding.MiaoBindingImpl
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.AnkoException
import java.io.Serializable


/**
 * Created by 10喵喵 on 2018/2/24.
 */
abstract class BaseFragment : SwipeBackFragment() {

    val binding = MiaoBindingImpl()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mac = render()
        val layout = layout()
        return attachToSwipeBack(if (mac != null) {
            binding.bindFns = mac.binding.bindFns
            mac.view
        } else if (layout != null) {
            inflater.inflate(layout, container, false)
        } else {
            View(context)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null)
            onReadInstanceState(savedInstanceState)
        initView()
    }

    open fun layout(): Int? = null

    open fun render(): MiaoAnkoContext<BaseFragment>? = null
    open fun initView() {} //初始化组件
    open fun bindInstanceState() {}
    open fun onReadInstanceState(state: Bundle) {}

    fun MiaoUI(init: MiaoAnkoContext<BaseFragment>.() -> Unit): MiaoAnkoContext<BaseFragment> =
            createMiaoAnkoContext(context!!, init)

    inline fun <reified T : BaseFragment> start(vararg params: Pair<String, Any>) {
        val fragment = T::class.java.newInstance()
        val argument = Bundle()
        if (params.isNotEmpty()) fillArguments(argument, params)
        fragment.arguments = argument
    }

    fun fillArguments(argument: Bundle, params: Array<out Pair<String, Any?>>) {
        params.forEach {
            val value = it.second
            when (value) {
                null -> argument.putSerializable(it.first, null as Serializable?)
                is Int -> argument.putInt(it.first, value)
                is Long -> argument.putLong(it.first, value)
                is CharSequence -> argument.putCharSequence(it.first, value)
                is String -> argument.putString(it.first, value)
                is Float -> argument.putFloat(it.first, value)
                is Double -> argument.putDouble(it.first, value)
                is Char -> argument.putChar(it.first, value)
                is Short -> argument.putShort(it.first, value)
                is Boolean -> argument.putBoolean(it.first, value)
                is Serializable -> argument.putSerializable(it.first, value)
                is Bundle -> argument.putBundle(it.first, value)
                is Parcelable -> argument.putParcelable(it.first, value)
                is Array<*> -> when {
                    value.isArrayOf<CharSequence>() -> argument.putCharSequenceArray(it.first, value as Array<CharSequence>)
                    value.isArrayOf<String>() -> argument.putStringArray(it.first, value as Array<String>)
                    value.isArrayOf<Parcelable>() -> argument.putParcelableArray(it.first, value as Array<Parcelable>)
                    else -> throw AnkoException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
                }
                is IntArray -> argument.putIntArray(it.first, value)
                is LongArray -> argument.putLongArray(it.first, value)
                is FloatArray -> argument.putFloatArray(it.first, value)
                is DoubleArray -> argument.putDoubleArray(it.first, value)
                is CharArray -> argument.putCharArray(it.first, value)
                is ShortArray -> argument.putShortArray(it.first, value)
                is BooleanArray -> argument.putBooleanArray(it.first, value)
                else -> throw AnkoException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }
            return@forEach
        }
    }
}