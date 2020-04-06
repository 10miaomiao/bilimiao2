package com.a10miaomiao.bilimiao.ui.user

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.bangumi.BangumiFragment
import com.a10miaomiao.bilimiao.ui.commponents.bangumiItemView
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.v
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout


class UserBangumiFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(vmid: Long): UserBangumiFragment {
            val fragment = UserBangumiFragment()
            val bundle = Bundle()
            bundle.putLong(ConstantUtil.ID, vmid)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val vmid by lazy { arguments!!.getLong(ConstantUtil.ID) }
    private lateinit var viewModel: UserBangumiViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel { UserBangumiViewModel(context!!, vmid) }
        return attachToSwipeBack(createUI().view)
    }

    private fun createUI() = UI {
        verticalLayout {
            headerView {
                title("Ta的追番")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                (+viewModel.loading) { isRefreshing = it }
                setOnRefreshListener { viewModel.refreshList() }
                recyclerView {
                    backgroundColor = config.blockBackgroundColor
                    miao(viewModel.list) {
                        layoutManager(LinearLayoutManager(context))
                        itemView { b ->
                            bangumiItemView(
                                    b.itemValue { cover },
                                    b.itemValue { title },
                                    "".v(),
                                    b.itemValue {
                                        if (is_started == 1) {
                                            if (finish == 1){
                                                "已完结"
                                            }else{
                                                "已更新到${newest_ep_index}话"
                                            }
                                        } else {
                                            "即将开播"
                                        }
                                    }
                            )
                        }
                        onItemClick { item, position ->
                            startFragment(
                                    BangumiFragment.newInstance(item.param)
                            )
                        }
                        addFootView {
                            loadMoreView {
                                layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                                (+viewModel.loadState){
                                    state = it
                                }
                            }
                        }
                        onLoadMore {
                            viewModel.pn++
                            viewModel.loadData()
                        }
                    }

                }
            }
        }
    }


}