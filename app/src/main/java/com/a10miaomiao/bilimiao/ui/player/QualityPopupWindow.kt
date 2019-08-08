package com.a10miaomiao.bilimiao.ui.player

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.flowLayout
import com.a10miaomiao.bilimiao.utils.selectableItemBackgroundBorderless
import com.a10miaomiao.miaoandriod.adapter.MiaoRecyclerViewAdapter
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView


class QualityPopupWindow(var context: Context, var anchor: View)
    : PopupWindow() {

    private var list = ArrayList<String>()

    private lateinit var mAdapter: MiaoRecyclerViewAdapter<*>
    var onCheckItemPositionChanged: ((value: String, position: Int) -> Unit)? = null

    var checkItemPosition = 0
        set(value) {
            field = value
            mAdapter.notifyDataSetChanged()
        }

    init {
        contentView = createUI().view
        setBackgroundDrawable(ColorDrawable(Color.argb(200, 0, 0, 0)))
        animationStyle = R.style.popwindow_anim_left
        isOutsideTouchable = true
        isFocusable = true
        width = context.dip(100f)
        height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    fun show() {
        this.showAtLocation(anchor, Gravity.RIGHT, 0, 0)
    }

    fun setData(data: List<String>) {
        list.clear()
        list.addAll(data)
        mAdapter.notifyDataSetChanged()
    }

    private fun createUI() = context.UI {
        frameLayout {
            recyclerView {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                mAdapter = miao(list) {
                    itemView { b ->
                        frameLayout {
                            textView {
                                selectableItemBackgroundBorderless()
                                textColor = Color.WHITE
                                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                                b.bindIndexed { item, index ->
                                    text = item
                                    textColor = if (checkItemPosition == index){
                                        context.config.themeColor
                                    }else{
                                        Color.WHITE
                                    }
                                }
                            }.lparams(matchParent, dip(48))
                        }
                    }
                    onItemClick { item, position ->
                        if (position != checkItemPosition) {
                            checkItemPosition = position
                            onCheckItemPositionChanged?.invoke(list[position], position)
                            dismiss()
                        }
                    }
                }
            }.lparams {
                gravity = Gravity.CENTER
            }
        }
    }
}