package com.a10miaomiao.bilimiao.comm.delegate.sheet

import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior

interface BottomSheetUi {
    var bottomSheetBehavior: BottomSheetBehavior<View>?
    var bottomSheetTitleView: TextView
    var bottomSheetMaskView: View
}