package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.commponents.dropMenuView
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.utils.startFragment
import com.a10miaomiao.miaoandriod.binding.bind
import org.jetbrains.anko.*
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.drawerLayout

class SearchResultFragment : Fragment() {

    lateinit var _text: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return render().view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    private fun render() = UI {
        drawerLayout {
            verticalLayout {
                linearLayout {
                    backgroundColor = Color.WHITE
                    padding = dip(5)
                    gravity = Gravity.CENTER_VERTICAL
                    textView {
                        textSize = 14f
                        setOnClickListener {
                            this@drawerLayout.openDrawer(Gravity.START)
                        }
                        text = "全部 · 全部时长 · 热度高>>>"
                    }.lparams(width = matchParent, weight = 1f)
                }.lparams(width = matchParent) { bottomMargin = dip(5) }

            }

            navigationView {
                verticalLayout {
                    textView("hello world")
                }
            }.lparams {
                height = matchParent
                width = dip(250)
                gravity = Gravity.START
            }
        }
    }
}