package com.a10miaomiao.bilimiao

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.fragment.app.FragmentContainerView
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.widget.scaffold.AppBarView
import com.a10miaomiao.bilimiao.widget.scaffold.ScaffoldView
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.AppBarBehavior
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.ContentBehavior
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.PlayerBehavior
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.DrawerBehavior
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.MaskBehavior
import com.a10miaomiao.bilimiao.widget.limitedFrameLayout
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.scaffold.behavior.MyBottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*


@OptIn(InternalSplittiesApi::class)
class MainUi(override val ctx: Context) : Ui {

    companion object {
        // 重启activiry时保持播放
        private var keepPlayerView: DanmakuVideoPlayer? = null
    }

    val mLeftContainerView = inflate<FragmentContainerView>(R.layout.left_fragment) {
        backgroundColor = config.windowBackgroundColor
        elevation = dip(20).toFloat()
        visibility = View.INVISIBLE
    }

    val mContainerView = inflate<View>(R.layout.container_fragment) {
        backgroundColor = Color.TRANSPARENT
    }

    val mSubContainerView = inflate<View>(R.layout.container_fragment_sub) {
        backgroundColor = Color.TRANSPARENT
    }

    val mAppBar = view<AppBarView>{
        setOnClickListener {  }
    }

    val mVideoPlayerView = keepPlayerView?.apply {
        try {
            (parent as? ViewGroup)?.removeAllViews()
            // 直接替换旧PlayerView的Context
            val clazz = View::class.java
            val mContextField = clazz.getDeclaredField("mContext")
            mContextField.isAccessible = true
            if (mContextField.get(this) is Context) {
                mContextField.set(this, ctx)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } ?: inflate<DanmakuVideoPlayer>(R.layout.include_palyer2) {
        keepPlayerView = this
    }

    val mPlayerLayout = frameLayout {
        backgroundColor = 0xFF000000.toInt()
        elevation = dip(19).toFloat()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            outlineSpotShadowColor = config.shadowColor
        }

        val completionView = inflate<RelativeLayout>(R.layout.include_completion_box)
        val errorMessageView = inflate<RelativeLayout>(R.layout.include_error_message_box)
        val areaLimitView = inflate<RelativeLayout>(R.layout.include_area_limit_box)
        val loadingView = inflate<FrameLayout>(R.layout.include_player_loading)

        addView(mVideoPlayerView, lParams(matchParent, matchParent))
        addView(completionView, lParams(matchParent, matchParent))
        addView(errorMessageView, lParams(matchParent, matchParent))
        addView(areaLimitView, lParams(matchParent, matchParent))
        addView(loadingView, lParams(matchParent, matchParent))
    }

    var mMaskView = view<View> {
        setBackgroundResource(R.color.black)
        elevation = dip(20).toFloat()
        alpha = 0f
        visibility = View.GONE
    }

    override val root = view<ScaffoldView>() {
        orientation = resources.configuration.orientation
        backgroundColor = config.blockBackgroundColor

        addView(mContainerView, lParams {
            behavior = ContentBehavior(ctx, null)
            width = matchParent
            height = matchParent
        })

        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val enableSubContent = prefs.getBoolean(SettingConstants.FLAGS_SUB_CONTENT_SHOW, false)
        mAppBar.enableSubContent = enableSubContent
        if (enableSubContent) {
            addView(mSubContainerView, lParams {
                behavior = ContentBehavior(ctx, null).let {
                    it.isSub = true
                    it
                }
                width = matchParent
                height = matchParent
            })
        }

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
    }

}