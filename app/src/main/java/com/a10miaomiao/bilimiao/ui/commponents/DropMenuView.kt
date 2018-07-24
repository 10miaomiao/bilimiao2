package com.a10miaomiao.bilimiao.ui.commponents

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.MenuItem
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.miaoandriod.MiaoAnkoContext
import com.a10miaomiao.miaoandriod.MiaoView
import com.a10miaomiao.miaoandriod.bind
import org.jetbrains.anko.button
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.textView

class DropMenuView(context: Context) : MiaoView(context) {

    var text by binding.miao("")
    var ico by binding.miao( R.drawable.ic_arrow_drop_down_24dp)

    val popupMenu = PopupMenu(context, this)

    init {
        onCreateView()
    }

    fun onMenuItemClick(listener: (menu: MenuItem) -> Unit){
        popupMenu.setOnMenuItemClickListener {
            text = it.title.toString()
            it.isChecked = true
            listener(it)
             false
        }
    }

    override fun render() = MiaoUI {
        linearLayout {
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener {
                popupMenu.show()
            }
            textView {
                bind (this@DropMenuView::text){ text = it }
            }
            imageView{
                bind (::ico){ setImageResource(it) }
            }
        }
    }
}