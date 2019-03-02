package com.a10miaomiao.miaoandriod

import android.content.Context
import android.support.annotation.LayoutRes
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.a10miaomiao.miaoandriod.anko.MiaoAnkoContext
import com.a10miaomiao.miaoandriod.anko.MiaoUI
import com.a10miaomiao.miaoandriod.binding.MiaoBindingImpl
import org.jetbrains.anko.matchParent

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

