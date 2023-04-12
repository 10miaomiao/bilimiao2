package com.a10miaomiao.bilimiao.page.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bilibili.app.dynamic.v2.ModuleOuterClass
import cn.a10miaomiao.miao.binding.android.view._tag
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.commponents.dynamic.dynamicCardView
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.recyclerviewAtViewPager2
import com.a10miaomiao.bilimiao.widget.wrapInNestedScrollableHostLayout
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class DynamicFragment: RecyclerViewFragment(), DIAware {

    companion object {
        fun newFragmentInstance(): DynamicFragment {
            val fragment = DynamicFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<DynamicViewModel>(di)

    private val themeDelegate by instance<ThemeDelegate>()

    private var themeId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (themeDelegate.getThemeResId() != themeId) {
            ui.cleanCacheView()
            themeId = themeDelegate.getThemeResId()
        }
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
//        val item = viewModel.list.data[position]
//        val args = bundleOf(
//            MainNavGraph.args.id to item.param
//        )
//        if (item.goto == "av" || item.goto == "vertical_av") {
//            Navigation.findNavController(view)
//                .navigate(MainNavGraph.action.global_to_videoInfo, args)
//        } else {
//            BiliUrlMatcher.toUrlLink(view, item.uri)
//        }
    }

    private val handleAuthorClick = View.OnClickListener {
        val tag = it.tag
        if (tag is Pair<*, *>
            && tag.first is Int
            && tag.second is String
        ) {
            val (type, id) = tag
            val args = bundleOf(
                MainNavGraph.args.id to id
            )
            when(type) {
                ModuleOuterClass.ModuleDynamicType.mdl_dyn_archive_VALUE -> {
                    Navigation.findNavController(it)
                        .navigate(MainNavGraph.action.global_to_user, args)
                }
                ModuleOuterClass.ModuleDynamicType.mdl_dyn_pgc_VALUE -> {
                    Navigation.findNavController(it)
                        .navigate(MainNavGraph.action.global_to_bangumiDetail, args)
                }
                else -> {
                    toast("未知跳转类型")
                }
            }
        }
    }

    private val handleDynamicContentClick = View.OnClickListener {
        val index = it.tag
        if (index is Int && index in viewModel.list.data.indices) {
            val item = viewModel.list.data[index]
            val args = bundleOf(
                MainNavGraph.args.id to item.dynamicContent.id
            )
            when(item.dynamicType) {
                ModuleOuterClass.ModuleDynamicType.mdl_dyn_archive_VALUE -> {
                    Navigation.findNavController(it)
                        .navigate(MainNavGraph.action.global_to_videoInfo, args)
                }
                ModuleOuterClass.ModuleDynamicType.mdl_dyn_pgc_VALUE -> {
                    Navigation.findNavController(it)
                        .navigate(MainNavGraph.action.global_to_bangumiDetail, args)
                }
                else -> {
                    toast("未知跳转类型")
                }
            }
        }
    }

    val itemUi = miaoBindingItemUi<DynamicViewModel.DataInfo> { item, index ->
        dynamicCardView(
            dynamicType = item.dynamicType,
            mid = item.mid,
            name = item.name,
            face = item.face,
            labelText = item.labelText,
            like = item.like,
            reply = item.reply,
            contentView = videoItem(
                title = item.dynamicContent.title,
                pic = item.dynamicContent.pic,
                remark = item.dynamicContent.remark,
            ).apply {
                _tag = index
                miaoEffect(null) {
                    setOnClickListener(handleDynamicContentClick)
                }
//                _show = item.dynamicType == ModuleOuterClass.ModuleDynamicType.mdl_dyn_archive
            },
            onAuthorClick = handleAuthorClick,
        ).apply {
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
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
                    mLayoutManager = _miaoLayoutManage(
                        LinearLayoutManager(requireContext())
                    )

                    val mAdapter = _miaoAdapter(
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
                        )..lParams(matchParent, wrapContent) {
                            bottomMargin = contentInsets.bottom
                        }

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