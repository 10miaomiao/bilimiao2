package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.utils.attr
import com.a10miaomiao.miaoandriod.MiaoView
import kotlinx.android.synthetic.main.include_header_bar.view.*
import org.jetbrains.anko.backgroundResource

class HeaderView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MiaoView(context, attrs, defStyleAttr) {

    override fun layout() = R.layout.include_header_bar

    init {
        onCreateView()
        val statusBarHeight = getStatusBarHeight()
        backgroundResource = context.attr(R.attr.colorPrimary)
        setPadding(0, statusBarHeight, 0, 0)
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen",
                "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
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