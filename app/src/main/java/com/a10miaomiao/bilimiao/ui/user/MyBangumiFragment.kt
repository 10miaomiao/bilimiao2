package com.a10miaomiao.bilimiao.ui.user

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.bangumi.BangumiFragment
import com.a10miaomiao.bilimiao.ui.commponents.bangumiItemView
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.ValueManager
import com.a10miaomiao.miaoandriod.adapter.miao
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout


class MyBangumiFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(vmid: Long): MyBangumiFragment {
            val fragment = MyBangumiFragment()
            val bundle = Bundle()
            bundle.putLong(ConstantUtil.ID, vmid)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val vmid by lazy { arguments!!.getLong(ConstantUtil.ID) }
    private lateinit var viewModel: MyBangumiViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel { MyBangumiViewModel(context!!, vmid) }
        return attachToSwipeBack(createUI().view)
    }

    private fun createUI() = UI {
        verticalLayout {
            headerView {
                title("我的追番")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                (+viewModel.loading) { isRefreshing = it }
                setOnRefreshListener { viewModel.refreshList() }
                recyclerView {
                    miao(viewModel.list) {
                        layoutManager(LinearLayoutManager(context))
                        itemView { b ->
                            bangumiItemView(
                                    b.itemValue { cover },
                                    b.itemValue { title },
                                    b.itemValue { new_ep.index_show },
                                    b.itemValue { progress?.index_show ?: "还没看喵" }
                            )
                        }
                        onItemClick { item, position ->
                            startFragment(
                                    BangumiFragment.newInstance(item.season_id.toString())
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