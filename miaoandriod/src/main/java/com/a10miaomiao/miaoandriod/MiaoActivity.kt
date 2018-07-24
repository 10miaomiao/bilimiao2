package com.a10miaomiao.miaoandriod

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.SupportActivity
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import org.jetbrains.anko.AnkoContext
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

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