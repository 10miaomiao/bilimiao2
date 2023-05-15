package com.a10miaomiao.bilimiao.page.region

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
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.entity.region.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class RegionDetailsFragment : RecyclerViewFragment(), DIAware {

    companion object {
        const val TID = "tid"
        var count = 1
        fun newInstance(tid: Int): RegionDetailsFragment {
            val fragment = RegionDetailsFragment()
            val bundle = Bundle()
            bundle.putInt(TID, tid)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<RegionDetailsViewModel>(di)

    private val timeSettingStore: TimeSettingStore by di.instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun refreshList() {
        if (!viewModel.list.loading) {
            viewModel.refreshList()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycle.coroutineScope.launchWhenResumed {
            timeSettingStore.stateFlow.collect {
                if (
                    viewModel.timeFrom.diff(it.timeFrom)
                    || viewModel.timeTo.diff(it.timeTo)
                    || viewModel.rankOrder != it.rankOrder
                ) {
                    viewModel.timeFrom = it.timeFrom
                    viewModel.timeTo = it.timeTo
                    viewModel.rankOrder = it.rankOrder
                    viewModel.refreshList()
                }

            }
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        val args = VideoInfoFragment.createArguments(item.id)
        Navigation.findNavController(view)
            .navigate(VideoInfoFragment.actionId, args)
    }

    val itemUi = miaoBindingItemUi<RegionTypeDetailsInfo> { item, index ->
        videoItem (
            title = item.title,
            pic = item.pic,
            upperName = item.author,
            playNum = item.play,
            damukuNum = item.video_review,
        )
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)
        verticalLayout {
            views {
                +recyclerView {
                    backgroundColor = config.windowBackgroundColor
                    mLayoutManager = _miaoLayoutManage(
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

