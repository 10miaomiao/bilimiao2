package com.a10miaomiao.bilimiao.page.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.miao.binding.Bind
import cn.a10miaomiao.miao.binding.MiaoBinding
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoRef
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.home.RecommendCardInfo
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.video.miniVideoItem
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.recyclerviewAtViewPager2
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*

class RecommendFragment: RecyclerViewFragment(), DIAware {

    companion object {
        fun newFragmentInstance(): RecommendFragment {
            val fragment = RecommendFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<RecommendViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun refreshList() {
        if (!viewModel.list.loading) {
            viewModel.refreshList()
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val nav = Navigation.findNavController(view)
        val item = viewModel.list.data[position]

        if (item.goto == "av" || item.goto == "vertical_av") {
            val args = VideoInfoFragment.createArguments(item.param)
            nav.navigate(VideoInfoFragment.actionId, args)
        } else if (item.goto == "bangumi") {
            nav.navigateToCompose(BangumiDetailPage()) {
                epId set item.param
            }
        } else if (!BiliNavigation.navigationTo(view, item.uri)){
            BiliNavigation.navigationToWeb(requireActivity(), item.uri)
        }
    }

    val itemUi = miaoBindingItemUi<RecommendCardInfo> { item, index ->
        if (viewModel.listStyle == 0) {
            videoItem (
                title = item.title,
                pic =item.cover,
                upperName = item.args.up_name,
                playNum = item.cover_left_text_1,
                damukuNum = item.cover_left_text_2,
                duration = item.cover_right_text,
            )
        } else {
            miniVideoItem (
                title = item.title,
                pic =item.cover,
                upperName = item.args.up_name,
                playNum = item.cover_left_text_1,
                damukuNum = item.cover_left_text_2,
                duration = item.cover_right_text,
            )
        }
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        verticalLayout {
//            _leftPadding = contentInsets.left + config.pagePadding
//            _rightPadding = contentInsets.right + config.pagePadding
//            _topPadding = config.pagePadding
//            _bottomPadding = contentInsets.bottom

            views {
                +recyclerviewAtViewPager2 {
                    backgroundColor = config.windowBackgroundColor
                    miaoEffect(viewModel.listStyle) {
                        mLayoutManager = if (viewModel.listStyle == 0) {
                            GridAutofitLayoutManager(requireContext(), dip(300))
                        } else {
                            GridAutofitLayoutManager(requireContext(), dip(180))
                        }
                        layoutManager = mLayoutManager
                    }
                    val mAdapter =_miaoAdapter(
                        items = viewModel.list.data,
                        itemUi = itemUi,
                    ) {
                        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        setOnItemClickListener(handleItemClick)
                        loadMoreModule.setOnLoadMoreListener {
                            viewModel.loadMode()
                        }
                    }
                    footerViews(mAdapter) {
                        +listStateView(
                            when {
                                viewModel.triggered -> ListState.NORMAL
                                viewModel.list.loading -> ListState.LOADING
                                viewModel.list.fail -> ListState.FAIL
                                viewModel.list.finished -> ListState.NOMORE
                                else -> ListState.NORMAL
                            },
                            viewModel::tryAgainLoadData,
                        ).apply {
                            _bottomPadding = contentInsets.bottom
                        }..lParams(matchParent, wrapContent)
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
