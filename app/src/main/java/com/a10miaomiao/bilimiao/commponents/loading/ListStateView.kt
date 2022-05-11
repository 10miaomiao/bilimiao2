package com.a10miaomiao.bilimiao.commponents.loading

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._tag
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm.progressBar
import com.a10miaomiao.bilimiao.comm.views
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.padding

enum class ListState {
    LOADING, NOMORE, FAIL, NORMAL, EMPTY
}

fun MiaoUI.listStateView(
    state: ListState,
    onFailRefreshClick: (() -> Unit)? = null,
    initLayout: (FrameLayout.() -> Unit)? = null,
): View {
    return frameLayout {
        initLayout?.invoke(this)
        _tag = state
        miaoEffect(onFailRefreshClick) {
            onFailRefreshClick?.let { refreshClick ->
                setOnClickListener {
                    val viewTag = tag
                    if (viewTag is ListState && viewTag == ListState.FAIL) {
                        refreshClick.invoke()
                    }
                }
            }
        }
        views {
            +horizontalLayout {
                gravity = Gravity.CENTER
                padding = dip(10)

                views {
                    +progressBar {
                        _show = state == ListState.LOADING
                    }..lParams {
                        width = dip(20)
                        height = dip(20)
                        rightMargin = dip(5)
                    }

                    +textView {
                        _text = when (state) {
                            ListState.LOADING -> "加载中"
                            ListState.NOMORE -> "下面没有了"
                            ListState.FAIL -> "无法连接到御坂网络"
                            ListState.NORMAL -> ""
                            ListState.EMPTY -> "空空如也"
                        }
                    }

                }
            }..lParams(matchParent, matchParent)
        }
    }
}