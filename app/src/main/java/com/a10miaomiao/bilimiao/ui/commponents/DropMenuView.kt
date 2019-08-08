package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.MenuItem
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.utils.selectableItemBackgroundBorderless
import com.a10miaomiao.miaoandriod.MiaoLiveData
import com.a10miaomiao.miaoandriod.MiaoView
import org.jetbrains.anko.UI
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.textView

class DropMenuView(context: Context) : MiaoView(context) {

    private val _text = MiaoLiveData("")
    var text
        get() = -_text
        set(value) {
            _text set value
        }

    private val _ico = MiaoLiveData(R.drawable.ic_arrow_drop_down_24dp)
    var ico
        get() = -_ico
        set(value) {
            _ico set value
        }

    val popupMenu = PopupMenu(context, this)

    init {
        addView(render().view)
    }

    fun onMenuItemClick(listener: (menu: MenuItem) -> Unit) {
        popupMenu.setOnMenuItemClickListener {
            text = it.title.toString()
            it.isChecked = true
            listener(it)
            false
        }
    }

    private fun render() = context.UI {

        linearLayout {
            gravity = Gravity.CENTER_VERTICAL

            setOnClickListener {
                popupMenu.show()
            }
            textView {
                (+_text){ text = it }
            }
            imageView {
                selectableItemBackgroundBorderless()
                (+_ico){ setImageResource(it) }
            }
        }
    }
}