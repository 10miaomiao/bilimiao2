package com.a10miaomiao.bilimiao

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentContainerView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.comm.behavior.AppBarBehavior
import com.a10miaomiao.bilimiao.widget.comm.behavior.ContentBehavior
import com.a10miaomiao.bilimiao.widget.comm.behavior.PlayerBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.material.hidden


//inline infix fun View.
@OptIn(InternalSplittiesApi::class)
class MainUi(override val ctx: Context) : Ui {

    val bottomSheetViewID = 123

    val mContainerView = inflate<FragmentContainerView>(R.layout.container_fragment) {
        backgroundColor = 0xFFF2F2F2L.toInt()
    }

    val mBottomSheetView = inflate<FragmentContainerView>(R.layout.bottom_sheet_fragment) {
        backgroundColor = 0xFFF2F2F2L.toInt()
    }

    val mAppBar = view<AppBarView>{ }

    val mPlayerLayout = inflate<FrameLayout>(R.layout.include_palyer)

    val mBottomSheetLayout = frameLayout {
        elevation = dip(4).toFloat()

        addView(mBottomSheetView, lParams {
            width = matchParent
            height = dip(500)
            gravity = Gravity.BOTTOM
        })

    }

    override val root = view<ScaffoldView>() {
        orientation = resources.configuration.orientation
        backgroundColor = 0xFFF2F2F2L.toInt()

        addView(mPlayerLayout, lParams {
            behavior = PlayerBehavior(ctx, null)
            width = wrapContent
            height = wrapContent
        })
        addView(mContainerView, lParams {
            behavior = ContentBehavior(ctx, null)
            width = matchParent
            height = matchParent
        })
        addView(mAppBar, lParams {
            behavior = AppBarBehavior(ctx, null)
            width = matchParent
            height = wrapContent
        })
        addView(mBottomSheetLayout, lParams {
            height = matchParent
            width = matchParent
            behavior = BottomSheetBehavior<View>(ctx, null)
        })

    }

}