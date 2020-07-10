package com.a10miaomiao.bilimiao.ui.filter

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.upper.UpperInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import io.reactivex.disposables.Disposable
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI

class FilterUpperFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(): FilterUpperFragment {
            val fragment = FilterUpperFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var filterStore: FilterStore
    lateinit var viewModel: FilterUpperViewModel

    private val rxBus = RxBus()
    private val disposable = arrayListOf<Disposable>()

    private val UPDATE_LIST = "UPDATE_LIST"
    private val UPDATE_BUTTON = "UPDATE_BUTTON"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        filterStore = Store.from(context!!).filterStore
        viewModel = ViewModelProviders.of(this, newViewModelFactory { FilterUpperViewModel(context!!) })
                .get(FilterUpperViewModel::class.java)
        return attachToSwipeBack(render().view)
    }


    private fun render() = UI {
        verticalLayout {
            headerView {
                title("屏蔽Up主设置")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
//                inflateMenu(R.menu.add)
//                onMenuItemClick(onMenuItemClickListener)
            }

            frameLayout {
                recyclerView {
                    val divide = RecycleViewDivider(context, LinearLayoutManager.VERTICAL, 2, Color.BLUE)
                    addItemDecoration(divide)
                    miao(filterStore.filterUpperList, owner) {
                        layoutManager(LinearLayoutManager(context!!))
                        itemView { b ->
                            linearLayout {
                                padding = config.dividerSize
                                gravity = Gravity.CENTER_VERTICAL
                                selectableItemBackground()

                                checkBox {
                                    rightPadding = config.dividerSize
                                    b.bindIndexed { item, index ->
                                        isChecked = viewModel.selectedList.contains(index)
                                        setOnCheckedChangeListener { buttonView, isChecked ->
                                            viewModel.selectedChange(index, isChecked)
                                            rxBus.send(UPDATE_BUTTON)
                                        }
                                    }
                                }
                                textView {
                                    textSize = 20f
                                    b.bind { text = "${it.name}(uid:${it.mid})" }
                                }.lparams { weight = 1f }
                            }.lparams(matchParent, wrapContent)
                        }

                        filterStore.filterUpperList.observe(owner, Observer {
                            viewModel.unSelectAll()
                            rxBus.send(UPDATE_BUTTON)
                            val count = filterStore.filterUpperCount
                            visibility = if (count > 0) {
                                this@recyclerView.scrollToPosition(count - 1)
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                        })
                        disposable += rxBus.on(UPDATE_LIST) {
                            notifyDataSetChanged()
                        }
                    }

                }

                verticalLayout {

                    textView("空空如也") {
                        gravity = Gravity.CENTER
                    }
                    textView("似乎没有讨厌的人")

                    filterStore.filterUpperList.observe(owner, Observer {
                        visibility = if (filterStore.filterUpperCount == 0)
                            View.VISIBLE
                        else
                            View.GONE
                    })

                }.lparams(wrapContent, wrapContent) {
                    gravity = Gravity.CENTER
                }
            }.lparams(matchParent, matchParent) {
                weight = 1f
            }

            linearLayout {
                backgroundColor = config.blockBackgroundColor
                gravity = Gravity.CENTER_VERTICAL

                textView {
                    gravity = Gravity.CENTER
                    selectableItemBackground()
                    val isSelectAll: () -> Boolean = {
                        filterStore.filterUpperCount ==
                                viewModel.selectedList.size
                    }
                    setOnClickListener {
                        if (isSelectAll())
                            viewModel.unSelectAll()
                        else
                            viewModel.selectAll(filterStore.filterWordCount)
                        rxBus.send(UPDATE_LIST)
                        rxBus.send(UPDATE_BUTTON)
                    }
                    disposable += rxBus.on(UPDATE_BUTTON) {
                        text = if (isSelectAll()) {
                            "全不选"
                        } else {
                            "全选"
                        }
                    }
                }.lparams {
                    height = matchParent
                    weight = 1f
                }

                textView {
                    text = "删除已选"
                    textColor = Color.RED
                    gravity = Gravity.CENTER
                    selectableItemBackground()
                    setOnClickListener {
                        val upperList = viewModel.selectedList
                                .map { filterStore.filterUpperList.value!![it].mid }
                        filterStore.deleteUpper(upperList)
                        viewModel.unSelectAll()
                    }
                }.lparams {
                    height = matchParent
                    weight = 1f
                }


            }.lparams(matchParent, dip(48))
        }
    }


}