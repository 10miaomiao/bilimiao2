package com.a10miaomiao.miaoandriod

import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import com.a10miaomiao.miaoandriod.anko.MiaoAnkoContext
import com.a10miaomiao.miaoandriod.binding.MiaoBindingImpl

abstract class MiaoActivity : AppCompatActivity() {

    val binding = MiaoBindingImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mac = render()
        val layout = layout()
        if (mac != null) {
            mac.binding = binding
            setContentView(mac.view)
        } else if (layout != null) {
            setContentView(layout)
        }
        initView()
    }

    @LayoutRes
    open fun layout(): Int? = null
    open fun render(): MiaoAnkoContext<Context>? = null
    open fun initView() {} //初始化组件
}