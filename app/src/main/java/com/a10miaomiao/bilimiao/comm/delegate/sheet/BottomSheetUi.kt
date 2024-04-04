package com.a10miaomiao.bilimiao.comm.delegate.sheet

import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior

interface BottomSheetUi {
    val bottomSheetView: View
    val bottomSheetBehavior: BottomSheetBehavior<View>?
    val bottomSheetTitleView: TextView
    val bottomSheetMaskView: View
}