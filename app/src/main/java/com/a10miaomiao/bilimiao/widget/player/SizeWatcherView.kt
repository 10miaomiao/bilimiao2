package com.a10miaomiao.bilimiao.widget.player

import android.content.Context
import android.util.AttributeSet
import android.view.View

class SizeWatcherView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onSizeChangedListener: (() -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
//        if (w != oldw && h != oldh){
//            DebugMiao.log("SizeWatcherView->[($w,$h),($oldw,$oldh)]")
        onSizeChangedListener?.invoke()
//        }
    }

}