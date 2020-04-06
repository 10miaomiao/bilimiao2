package com.a10miaomiao.bilimiao.ui.theme

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.home.HomeViewModel
import com.a10miaomiao.bilimiao.utils.ThemeUtil
import com.a10miaomiao.bilimiao.utils.newViewModelFactory
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import com.a10miaomiao.miaoandriod.adapter.miao
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI


class ThemeFragment : SwipeBackFragment() {

    lateinit var viewModel: ThemeViewModel
    lateinit var themeUtil: ThemeUtil

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory {
            ThemeViewModel(context!!)
        }).get(ThemeViewModel::class.java)
        themeUtil = MainActivity.of(context!!).themeUtil
        val view = themeUtil.dynamicTheme(this, { render().view }, true)
        return attachToSwipeBack(view)
    }

    private val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {

        }

        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            themeUtil.setNight(p2)
        }
    }


    private fun render() = UI {
        val selected = themeUtil.theme
        verticalLayout {
            backgroundColor = config.windowBackgroundColor

            headerView {
                title("切换主题")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
            }

            linearLayout {
                verticalPadding = dip(10)
                textView("夜间模式：").lparams{
                    leftMargin = dip(20)
                }
                spinner {
                    val mAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item
                            , arrayOf("跟随系统", "关闭", "打开"))
                    mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    adapter = mAdapter

                    setSelection(themeUtil.getNight())
                    onItemSelectedListener = itemSelectedListener
                }
            }

            recyclerView {
                applyRecursively(ViewStyle.roundRect(dip(5)))
                backgroundColor = config.blockBackgroundColor

                layoutManager = LinearLayoutManager(context)
                miao(viewModel.list) {
                    itemView { b ->
                        linearLayout {
                            lparams(matchParent, dip(48))
                            selectableItemBackground()
                            gravity = Gravity.CENTER_VERTICAL

                            view {
                                applyRecursively(ViewStyle.circle)
                                b.bind { backgroundColor = it.color }
                            }.lparams(dip(24), dip(24)) {
                                horizontalMargin = dip(15)
                            }

                            textView {
                                b.bind {
                                    text = it.name
                                    textColor = it.color
                                }
                            }.lparams {
                                weight = 1f
                            }

                            textView {
                                b.bind { item ->
                                    if (viewModel.selected.value == item.theme) {
                                        text = "使用中"
                                        textColor = item.color
                                    } else {
                                        text = "使用"
                                        textColor = config.foregroundAlpha45Color
                                    }
                                }
                            }.lparams { rightMargin = dip(15) }

                        }
                    }
                    onItemClick { item, position ->
                        themeUtil.setTheme(item.theme)
                    }
                    selected.observe(owner, Observer { notifyDataSetChanged() })
                }
            }.lparams(matchParent, matchParent)

        }
    }

}