package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.MenuRes
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.utils.attr
import com.a10miaomiao.bilimiao.utils.getStatusBarHeight
import kotlinx.android.synthetic.main.include_header_bar.view.*
import org.jetbrains.anko.backgroundResource

class HeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.include_header_bar, this)
        val statusBarHeight = getStatusBarHeight()
        backgroundResource = context.attr(R.attr.colorPrimary)
        setPadding(0, statusBarHeight, 0, 0)
    }

    fun inflateMenu(@MenuRes resId: Int) {
        mToolbar.inflateMenu(resId)
    }

    fun onMenuItemClick(listener: Toolbar.OnMenuItemClickListener) {
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