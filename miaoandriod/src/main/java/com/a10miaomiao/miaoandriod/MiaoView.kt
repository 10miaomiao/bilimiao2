package com.a10miaomiao.miaoandriod

import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.UI
import org.jetbrains.anko.matchParent
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

open class MiaoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    var binding = MiaoBindingImpl()

    fun onCreateView(){
        val mac = render()
        val layout = layout()
        if (mac != null) {
            binding.bindFns = mac.binding.bindFns
            addView(mac.view, matchParent, matchParent)
        } else if (layout != null) {
            View.inflate(context, layout, this)
        }
    }


    @LayoutRes open fun layout(): Int? = null
    open fun render(): MiaoAnkoContext<Context>? = null

    protected fun MiaoUI(init: MiaoAnkoContext<Context>.() -> Unit): MiaoAnkoContext<Context> = context!!.MiaoUI(init)
}

