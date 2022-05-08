package com.a10miaomiao.bilimiao.page.rank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bilibili.app.show.v1.RankOuterClass
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.region.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.region.RegionDetailsFragment
import com.a10miaomiao.bilimiao.page.region.RegionDetailsViewModel
import com.a10miaomiao.bilimiao.store.TimeSettingStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.flow.collect
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class RankDetailFragment : Fragment(), DIAware {

    companion object {
        const val TID = "tid"
        var count = 1
        fun newInstance(tid: Int): RankDetailFragment {
            val fragment = RankDetailFragment()
            val bundle = Bundle()
            bundle.putInt(TID, tid)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<RankDetailViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }


    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        val args = bundleOf(
            MainNavGraph.args.id to item.param
        )
        Navigation.findNavController(view)
            .navigate(MainNavGraph.action.global_to_videoInfo, args)
    }

    val itemUi = miaoBindingItemUi<RankOuterClass.Item> { item, index ->
        videoItem (
            title = item.title,
            pic = item.cover,
            upperName = item.name,
            playNum = item.play.toString(),
            damukuNum = item.danmaku.toString(),
        )
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)
        verticalLayout {
            views {
                +recyclerView {
                    backgroundColor = config.windowBackgroundColor
                    _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
                    )

                    val footerView = listStateView(
                        when {
                            viewModel.triggered -> ListState.NORMAL
                            viewModel.list.loading -> ListState.LOADING
                            viewModel.list.fail -> ListState.FAIL
                            viewModel.list.finished -> ListState.NOMORE
                            else -> ListState.NORMAL
                        }
                    )
                    footerView..lParams(matchParent, wrapContent) {
                        bottomMargin = contentInsets.bottom
                    }

                    _miaoAdapter(
                        items = viewModel.list.data,
                        itemUi = itemUi,
                    ) {
                        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        setOnItemClickListener(handleItemClick)
                        loadMoreModule.setOnLoadMoreListener {
                            viewModel.loadMode()
                        }
                        addFooterView(footerView)
                    }
                }.wrapInSwipeRefreshLayout {
                    setColorSchemeResources(config.themeColorResource)
                    setOnRefreshListener(handleRefresh)
                    _isRefreshing = viewModel.triggered
                }..lParams(matchParent, matchParent)
            }
        }
    }
}