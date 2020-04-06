package com.a10miaomiao.bilimiao.ui.user

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.mediaItemView
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class FavFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(mid: Long): FavFragment {
            val fragment = FavFragment()
            val bundle = Bundle()
            bundle.putLong(ConstantUtil.ID, mid)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val mid by lazy { arguments!!.getLong(ConstantUtil.ID) }
    private lateinit var viewModel: FavViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel { FavViewModel(context!!, mid) }
        return attachToSwipeBack(createUI().view)
    }

    private fun createUI() = UI {
        val userStore = MainActivity.of(context!!).userStore
        verticalLayout {
            lparams(matchParent, matchParent)
            headerView {
                if (userStore.isSelf(mid)) {
                    title("我的收藏")
                } else {
                    title("Ta的收藏")
                }
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                (+viewModel.loading) { isRefreshing = it }
                setOnRefreshListener { viewModel.refreshList() }
                recyclerView {
                    backgroundColor = config.blockBackgroundColor
                    createAdapter()
                }
            }
        }
    }

    private fun RecyclerView.createAdapter() = miao(viewModel.list) {
        layoutManager(GridLayoutManager(context, 2))
        itemView {
            mediaItemView(
                    it.itemValue { cover },
                    it.itemValue { title },
                    it.itemValue { "共${media_count}个视频" }
            )
        }
        onItemClick { item, position ->
            startFragment(FavDetailsFragment.newInstance(item.id, item.title))
        }
    }


}