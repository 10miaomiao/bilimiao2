package com.a10miaomiao.bilimiao.ui.filter

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.rank.RankCategoryDetailsViewModel
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import io.reactivex.disposables.Disposable


class FilterWorldFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(): FilterWorldFragment {
            val fragment = FilterWorldFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    lateinit var filterStore: FilterStore
    lateinit var viewModel: FilterWorldViewModel

    private val rxBus = RxBus()
    private val disposable = arrayListOf<Disposable>()

    private val UPDATE_LIST = "UPDATE_LIST"
    private val UPDATE_BUTTON = "UPDATE_BUTTON"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        filterStore = Store.from(context!!).filterStore
        viewModel = ViewModelProviders.of(this, newViewModelFactory { FilterWorldViewModel(context!!) })
                .get(FilterWorldViewModel::class.java)
        return attachToSwipeBack(render().view)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.forEach { it.dispose() }
    }

    private val onMenuItemClickListener = Toolbar.OnMenuItemClickListener {
        startFragment(AddWorldFragment.newInstance())
        true
    }


    private fun render() = UI {
        verticalLayout {
            headerView {
                title("屏蔽标题关键字设置")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
                inflateMenu(R.menu.add)
                onMenuItemClick(onMenuItemClickListener)
            }

            frameLayout {
                recyclerView {
                    val divide = RecycleViewDivider(context, LinearLayoutManager.VERTICAL, 2, Color.BLUE)
                    addItemDecoration(divide)
                    miao(filterStore.filterWordList, owner) {
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
                                    b.bind { text = it }
                                }.lparams { weight = 1f }
                            }.lparams(matchParent, wrapContent)
                        }

                        filterStore.filterWordList.observe(owner, Observer {
                            viewModel.unSelectAll()
                            rxBus.send(UPDATE_BUTTON)
                            val count = filterStore.filterWordCount
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
                        onItemClick { item, position ->
                            startFragment(EditWorldFragment.newInstance(item))
                        }
                    }

                }

                verticalLayout {

                    textView("空空如也") {
                        gravity = Gravity.CENTER
                    }
                    textView("去添加关键字")

                    filterStore.filterWordList.observe(owner, Observer {
                        visibility = if (filterStore.filterWordCount == 0)
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
                        filterStore.filterWordCount ==
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
                        val keywordList = viewModel.selectedList
                                .map { filterStore.filterWordList.value!![it] }
                        filterStore.deleteWord(keywordList)
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