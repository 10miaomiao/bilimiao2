package com.a10miaomiao.bilimiao.comm.dialogx

import android.widget.RelativeLayout
import com.kongzue.dialogx.dialogs.PopTip

fun PopTip.showTop(): PopTip {
    // 修改弹出位置
    (dialogImpl.boxBody.layoutParams as? RelativeLayout.LayoutParams)?.also {
        it.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        it.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        it.setMargins(
            it.leftMargin,
            dip2px(100f),
            it.rightMargin,
            0
        )
    }
    return this
}