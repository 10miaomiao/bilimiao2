package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.MenuRes
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.utils.attr
import com.a10miaomiao.bilimiao.utils.getStatusBarHeight
import com.a10miaomiao.miaoandriod.MiaoView
import kotlinx.android.synthetic.main.include_header_bar.view.*
import org.jetbrains.anko.backgroundResource
import kotlin.reflect.KFunction1

class HeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MiaoView(context, attrs, defStyleAttr) {

    override fun layout() = R.layout.include_header_bar

    init {
        onCreateView()
        val statusBarHeight = getStatusBarHeight()
        backgroundResource = context.attr(R.attr.colorPrimary)
        setPadding(0, statusBarHeight, 0, 0)
    }

    fun inflateMenu(@MenuRes resId: Int) {
        mToolbar.inflateMenu(resId)
    }

    fun onMenuItemClick(listener: KFunction1<@ParameterName(name = "menuItem") MenuItem, Boolean>) {
        mToolbar.setOnMenuItemClickListener(listener)
    }

    fun title(title: String) {
        mToolbar.title = title
    }

    fun navigationIcon(@DrawableRes resId: Int) {
        mToolbar.setNavigationIcon(resId)
    }

    fun navigationOnClick(listener: (v: View) -> Unit) {
        mToolbar.setNavigationOnClickListener(listener)
    }
}