package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.view.Gravity
import android.view.View
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.MiaoView
import org.jetbrains.anko.*

class LoadMoreView(context: Context) : MiaoView(context) {
    enum class State {
        LOADING, NOMORE, FAIL
    }

    private val _state = MiaoLiveData(State.LOADING)
    var state
        get() = -_state
        set(value) = _state set value

    init {
        addView(render().view)
    }

    private fun render() = context.UI {
        val observe = +_state
        linearLayout {
            lparams(matchParent, wrapContent)
            gravity = Gravity.CENTER
            padding = dip(10)
            progressBar {
                observe { visibility = if (state == State.LOADING) View.VISIBLE else View.GONE }
            }.lparams {
                width = dip(20)
                height = dip(20)
                rightMargin = dip(5)
            }
            textView("加载中") {
                observe {
                    text = when (state) {
                        State.LOADING -> "加载中"
                        State.NOMORE -> "下面没有了"
                        State.FAIL -> "无法连接到御坂网络"
                    }
                }
            }
        }
    }
}