package com.a10miaomiao.bilimiao

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.widget.scaffold.AppBarView
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.AppBarBehavior
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.ContentBehavior
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.PlayerBehavior
import com.a10miaomiao.bilimiao.comm.delegate.sheet.BottomSheetUi
import com.a10miaomiao.bilimiao.comm.shadowLayout
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.service.PlayerService
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.DrawerBehavior
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.MaskBehavior
import com.a10miaomiao.bilimiao.widget.limitedFrameLayout
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import kotlin.math.ceil
import kotlin.math.max


@OptIn(InternalSplittiesApi::class)
class MainUi(override val ctx: Context) : Ui, BottomSheetUi {


    val mLeftContainerView = inflate<FragmentContainerView>(R.layout.left_fragment) {
        backgroundColor = config.windowBackgroundColor
        elevation = dip(20).toFloat()
        visibility = View.INVISIBLE
    }

    val mContainerView = inflate<FragmentContainerView>(R.layout.container_fragment) {
        backgroundColor = Color.TRANSPARENT
    }

    val mBottomSheetView = inflate<FragmentContainerView>(R.layout.bottom_sheet_fragment) {
        backgroundColor = config.windowBackgroundColor
        setOnClickListener {  }
    }

    val mAppBar = view<AppBarView>{
        setOnClickListener {  }
    }

    val mPlayerLayout = frameLayout {
        backgroundColor = 0xFF000000.toInt()
        elevation = dip(20).toFloat()

        val videoPlayerView = PlayerService.selfInstance?.videoPlayerView?.apply {
            (parent as? ViewGroup)?.removeAllViews()
        } ?: inflate<DanmakuVideoPlayer>(R.layout.include_palyer2) {
            PlayerService.selfInstance?.videoPlayerView = this
        }

        val completionView = inflate<RelativeLayout>(R.layout.include_completion_box)
        val errorMessageView = inflate<RelativeLayout>(R.layout.include_error_message_box)
        val areaLimitView = inflate<RelativeLayout>(R.layout.include_area_limit_box)

        addView(videoPlayerView, lParams(matchParent, matchParent))
        addView(completionView, lParams(matchParent, matchParent))
        addView(errorMessageView, lParams(matchParent, matchParent))
        addView(areaLimitView, lParams(matchParent, matchParent))
    }

    var mBottomSheetTitleView = textView {
        gravity = Gravity.CENTER
    }

    val mBottomSheetLayout = frameLayout {
        elevation = dip(20).toFloat()
        setOnClickListener { }
//        translationY = dip(-200f)

        addView(limitedFrameLayout {
            maxHeight = dip(500)
            maxWidth = dip(600)

            addView(shadowLayout {
                backgroundColor = config.windowBackgroundColor
                val round = dip(10)
                setSpecialCorner(round, round, 0 ,0)

                addView(mBottomSheetView, lParams {
                    width = matchParent
                    height = matchParent
                })
                addView(bottomSheetTitleView, lParams {
                    width = matchParent
                    height = config.bottomSheetTitleHeight
                })
            }, lParams {
                width = matchParent
                height = matchParent
            })
        }, lParams {
            width = matchParent
            height = matchParent
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        })
    }

    var mBottomSheetBehavior: BottomSheetBehavior<View>? = null

    var mMaskView = view<View> {
        setBackgroundResource(R.color.black)
        elevation = dip(20).toFloat()
        alpha = 0f
        visibility = View.GONE
    }

    override val root = view<ScaffoldView>() {
        orientation = resources.configuration.orientation
        backgroundColor = config.windowBackgroundColor

        addView(mContainerView, lParams {
            behavior = ContentBehavior(ctx, null)
            width = matchParent
            height = matchParent
        })

        addView(mAppBar, lParams {
            behavior = AppBarBehavior(ctx, null)
            width = matchParent
            height = matchParent
        })

        addView(mMaskView, lParams {
            behavior = MaskBehavior(ctx, null)
            height = matchParent
            width = matchParent
        })

        addView(mPlayerLayout, lParams {
            behavior = PlayerBehavior(ctx, null)
            width = wrapContent
            height = wrapContent
        })

//        addView(frameLayout {
//            backgroundColor = config.blockBackgroundColor
//            elevation = dip(20).toFloat()
//        }, lParams {
//            behavior = PlayerDraggableBehavior(ctx, null)
//        })

        addView(mLeftContainerView, lParams {
            height = matchParent
            width = matchParent
            behavior = DrawerBehavior(ctx, null)
        })

        addView(mBottomSheetLayout, lParams {
            height = matchParent
            width = matchParent
            val b = BottomSheetBehavior<View>(ctx, null)

            behavior = b
            this@MainUi.mBottomSheetBehavior = b
        })
    }
    override val bottomSheetBehavior get() = mBottomSheetBehavior
    override val bottomSheetTitleView get() = mBottomSheetTitleView
    override val bottomSheetMaskView get() = mMaskView

}