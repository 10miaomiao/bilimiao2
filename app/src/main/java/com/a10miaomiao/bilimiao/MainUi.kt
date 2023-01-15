package com.a10miaomiao.bilimiao

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.a10miaomiao.bilimiao.widget.comm.AppBarView
import com.a10miaomiao.bilimiao.widget.comm.ScaffoldView
import com.a10miaomiao.bilimiao.widget.comm.behavior.AppBarBehavior
import com.a10miaomiao.bilimiao.widget.comm.behavior.ContentBehavior
import com.a10miaomiao.bilimiao.widget.comm.behavior.PlayerBehavior
import com.a10miaomiao.bilimiao.comm.delegate.sheet.BottomSheetUi
import com.a10miaomiao.bilimiao.comm.shadowLayout
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.service.PlayerService
import com.a10miaomiao.bilimiao.widget.limitedFrameLayout
import com.a10miaomiao.bilimiao.widget.player.DanmakuVideoPlayer
import com.a10miaomiao.bilimiao.widget.shadow.ShadowLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.constraintlayout.bottomOfParent
import splitties.views.dsl.constraintlayout.constraintLayout
import splitties.views.dsl.constraintlayout.lParams
import splitties.views.dsl.constraintlayout.parentId
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView


//inline infix fun View.
@OptIn(InternalSplittiesApi::class)
class MainUi(override val ctx: Context) : Ui, BottomSheetUi {

    val leftNavigationList = mutableListOf(
        NavigationListAdapter.NavigationItem(
            title = "当前页面",
            subtitle = "首页"
        ),
        NavigationListAdapter.NavigationItem(
            itemType = NavigationListAdapter.ITEM_DIVIDER,
            title = "",
        ),
        NavigationListAdapter.NavigationItem(
            title = "首页",
        ),
        NavigationListAdapter.NavigationItem(
            title = "番剧",
        ),
        NavigationListAdapter.NavigationItem(
            title = "动态",
        ),
        NavigationListAdapter.NavigationItem(
            itemType = NavigationListAdapter.ITEM_DIVIDER,
            title = "",
        ),
        NavigationListAdapter.NavigationItem(
            title = "我的收藏/订阅",
        ),
        NavigationListAdapter.NavigationItem(
            title = "历史记录",
        ),
        NavigationListAdapter.NavigationItem(
            title = "我的下载",
        ),
        NavigationListAdapter.NavigationItem(
            itemType = NavigationListAdapter.ITEM_DIVIDER,
            title = "",
        ),
        NavigationListAdapter.NavigationItem(
            title = "设置",
        ),
    )
    val leftNavigationAdapter = NavigationListAdapter(leftNavigationList)
    val leftNavigationView = recyclerView {
        backgroundColor= config.windowBackgroundColor
        layoutManager = LinearLayoutManager(ctx)
        adapter = leftNavigationAdapter
    }

    val mContainerView = inflate<View>(R.layout.container_fragment) {
        backgroundColor = config.windowBackgroundColor
    }
//    val mContainerView = view<DrawerLayout> {
//        addView(inflate(R.layout.container_fragment))
//        addView(leftNavigationView, DrawerLayout.LayoutParams(matchParent, matchParent).apply {
//            gravity = Gravity.LEFT
//        })
//        backgroundColor = config.windowBackgroundColor
//    }

    val mBottomSheetView = inflate<FragmentContainerView>(R.layout.bottom_sheet_fragment) {
        backgroundColor = config.windowBackgroundColor
        setOnClickListener {  }
    }

    val mAppBar = view<AppBarView>{
        backgroundColor = config.blockBackgroundColor
        elevation = dip(20).toFloat()
    }

    val mPlayerLayout = PlayerService.selfInstance?.videoPlayerView?.apply {
        (parent as? ViewGroup)?.removeAllViews()
    } ?: inflate<DanmakuVideoPlayer>(R.layout.include_palyer2) {
        backgroundColor = 0xFF000000.toInt()
        elevation = dip(20).toFloat()
        PlayerService.selfInstance?.videoPlayerView = this
    }

    override var bottomSheetTitleView = textView {
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

    override var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override var bottomSheetMaskView = view<View> {
        setBackgroundResource(R.color.black)
        elevation = dip(20).toFloat()
        alpha = 0f
        visibility = View.GONE
    }

    override val root = view<ScaffoldView>() {
        orientation = resources.configuration.orientation
        backgroundColor = 0xFFF2F2F2L.toInt()

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
        addView(bottomSheetMaskView, lParams {
            height = matchParent
            width = matchParent
        })
        addView(mPlayerLayout, lParams {
            behavior = PlayerBehavior(ctx, null)
            width = wrapContent
            height = wrapContent
        })
        addView(mBottomSheetLayout, lParams {
            height = matchParent
            width = matchParent
            val b = BottomSheetBehavior<View>(ctx, null)
            behavior = b
            this@MainUi.bottomSheetBehavior = b
        })

    }

    fun setNavigationTitle(
        title: String,
    ) {
        leftNavigationAdapter.setData(0, NavigationListAdapter.NavigationItem(
            title = "当前页面",
            subtitle = title.replace("\n", " "),
        ))
    }
}