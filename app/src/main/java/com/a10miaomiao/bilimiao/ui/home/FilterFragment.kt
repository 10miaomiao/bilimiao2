package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.flowLayout
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.filter.FilterUpperFragment
import com.a10miaomiao.bilimiao.ui.filter.FilterWorldFragment
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.ui.widget.flow.FlowAdapter
import com.a10miaomiao.bilimiao.ui.widget.flow.FlowLayout
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.RxBus
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import com.a10miaomiao.bilimiao.utils.startFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView


class FilterFragment : Fragment() {

    lateinit var filterStore: FilterStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        filterStore = Store.from(context!!).filterStore
        return MainActivity.of(context!!).dynamicTheme(this) { render().view }
    }

    private val onMenuItemClick = Toolbar.OnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
            R.id.search -> {
                startFragment(SearchFragment.newInstance())
            }
        }
        true
    }

    private fun render() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor
            headerView {
                title("屏蔽设置")
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    MainActivity.of(context!!).openDrawer()
                }
                inflateMenu(R.menu.search)
                onMenuItemClick(onMenuItemClick)
            }

            nestedScrollView {
                verticalLayout {

                    verticalLayout {
                        applyRecursively(ViewStyle.block)

                        textView("屏蔽标题关键字：")

                        val filterWordAdapter = MyAdapter(Transformations.map(filterStore.filterWordList) {
                            it.toList()
                        })
                        flowLayout {
                            setItemMargins(0, 0, dip(10), dip(8))
                            setAdapter(filterWordAdapter)
                        }.lparams {
                            verticalMargin = dip(10)
                        }

                        textView {
                            text = "空空如也"
                            filterStore.filterWordList.observe(owner, Observer {
                                visibility = if (filterStore.filterWordCount == 0) {
                                    View.VISIBLE
                                } else {
                                    View.GONE
                                }
                            })
                        }.lparams {
                            gravity = Gravity.CENTER
                            verticalMargin = dip(10)
                        }

                        textView {
                            text = "去设置"
                            textColorResource = config.themeColorResource
                            setOnClickListener {
                                val fragment = FilterWorldFragment.newInstance()
                                startFragment(fragment)
                            }
                        }.lparams {
                            gravity = Gravity.RIGHT
                        }

                    }.lparams(width = matchParent) { verticalMargin = config.dividerSize }


                    verticalLayout {
                        applyRecursively(ViewStyle.block)

                        textView("屏蔽up主：")

                        val filterWordAdapter = MyAdapter(Transformations.map(filterStore.filterUpperList) {
                            it.map { it.name }
                        })
                        flowLayout {
                            setItemMargins(0, 0, dip(10), dip(8))
                            setAdapter(filterWordAdapter)
                        }.lparams {
                            verticalMargin = dip(10)
                        }

                        textView {
                            text = "空空如也"
                            filterStore.filterUpperList.observe(owner, Observer {
                                visibility = if (filterStore.filterUpperCount == 0) {
                                    View.VISIBLE
                                } else {
                                    View.GONE
                                }
                            })
                        }.lparams {
                            gravity = Gravity.CENTER
                            verticalMargin = dip(10)
                        }

                        textView {
                            text = "去设置"
                            textColorResource = config.themeColorResource
                            setOnClickListener {
                                val fragment = FilterUpperFragment.newInstance()
                                startFragment(fragment)
                            }
                        }.lparams {
                            gravity = Gravity.RIGHT
                        }

                    }

                    textView("注：屏蔽之后，你将不会在时光机、搜索和排行榜的视频列表看到对应的视频") {
                        padding = dip(10)
                    }
                }

            }
        }
    }

    private inner class MyAdapter(data: LiveData<List<String>>) : FlowAdapter<String>(data.value) {

        init {
            data.observe(this@FilterFragment, Observer {
                dataList = it
                notifyDataSetChanged()
            })
        }

        override fun getView(position: Int, parent: FlowLayout) = parent.context!!.UI {
            frameLayout {
                applyRecursively(ViewStyle.roundRect(dip(5)))
                backgroundColor = config.windowBackgroundColor
                textView {
                    text = getItem(position)
                    padding = dip(5)
                }

            }
        }.view
    }

}