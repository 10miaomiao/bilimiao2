package com.a10miaomiao.bilimiao.page.bangumi

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.widget._textColorResource
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.model.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.bangumi.EpisodeInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.comm.MenuItemView
import com.a10miaomiao.bilimiao.widget.layout.DoubleColumnAutofitLayout
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class BangumiDetailFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "番剧详情"
        menus = listOf(
            myMenuItem {
                key = MenuKeys.more
                iconResource = R.drawable.ic_more_vert_grey_24dp
                title = "更多"
            },
            myMenuItem {
                key = MenuKeys.follow
                if (viewModel.isFollow) {
                    iconResource = R.drawable.ic_baseline_favorite_24
                    title = "已追番"
                } else  {
                    iconResource = R.drawable.ic_outline_favorite_border_24
                    title = "追番"
                }
            },
        )
    }

    override fun onMenuItemClick(view: MenuItemView) {
        super.onMenuItemClick(view)
        when (view.prop.key) {
            MenuKeys.more -> {
                // 更多
                val pm = BangumiMorePopupMenu(
                    activity = requireActivity(),
                    anchor = view,
                    viewModel = viewModel
                )
                pm.show()
            }
            MenuKeys.follow -> {
                // 追番
                viewModel.followSeason()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui }) {
        bindSingleton<MyPage> { this@BangumiDetailFragment }
    }

    private val viewModel by diViewModel<BangumiDetailViewModel>(di)

    private val windowStore: WindowStore by instance()

    private val basePlayerDelegate: BasePlayerDelegate by instance()

    private val playerStore: PlayerStore by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshData()
    }

    private val handleItemSelected = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.updateSeasonsIndex(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    private val handleEpisodeItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.episodes[position]
        val playerSource = BangumiPlayerSource(
            item.section_id,
            item.ep_id,
            item.cid.toString(),
            item.index_title
        )
        basePlayerDelegate.openPlayer(playerSource)
    }

    private val handleMoreClick = View.OnClickListener {
        viewModel.detailInfo?.let { info ->
            val nav = Navigation.findNavController(requireActivity(), R.id.nav_bottom_sheet_fragment)
            val args = bundleOf(
                MainNavGraph.args.id to info.season_id.toString(),
                MainNavGraph.args.pages to viewModel.episodes,
            )
            nav.navigate(MainNavGraph.action.global_to_bangumiPages, args)
        }
    }

    val episodeItemUi = miaoBindingItemUi<EpisodeInfo> { item, index ->
        verticalLayout {
            setBackgroundResource(R.drawable.shape_corner)
            layoutParams = ViewGroup.MarginLayoutParams(wrapContent, matchParent).apply {
                rightMargin = dip(5)
            }
            horizontalPadding = dip(10)
            verticalPadding = dip(10)
            gravity = Gravity.LEFT

            val isSelect = viewModel.isPlaying(item.ep_id)
            val isEmptyTitle = item.index_title.isEmpty()
            _isEnabled = !isSelect

            views {
                +textView {
                    textColorResource = R.color.text_black
                    _text = if (isEmptyTitle) {
                        item.index
                    } else {
                        "第${item.index}集"
                    }
                    _textColorResource = if (isSelect) {
                        config.themeColorResource
                    } else {
                        R.color.text_black
                    }
                }

                +textView {
                    textColorResource = R.color.text_black
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.LEFT
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START

                    _show = !isEmptyTitle
                    _text = item.index_title
                    _textColorResource = if (isSelect) {
                        config.themeColorResource
                    } else {
                        R.color.text_black
                    }
                }..lParams(dip(120), wrapContent) {
                    topMargin = dip(5)
                }
            }
        }
    }

    fun MiaoUI.headerView(): View {
        val detailInfo = viewModel.detailInfo
        return verticalLayout {
            backgroundColor = config.blockBackgroundColor
            padding = config.pagePadding
            gravity = gravityCenter
            apply(ViewStyle.roundRect(dip(10)))

            views {

                +imageView {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    _network(detailInfo?.cover)
                    apply(ViewStyle.roundRect(dip(10)))
                }..lParams(dip(120), dip(166))

                +textView {
                    textSize = 20f
                    setTextColor(config.foregroundColor)
                    maxLines = 2
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = gravityCenter
                    _text = detailInfo?.title ?: ""
                }..lParams(wrapContent, wrapContent) {
                    topMargin = config.pagePadding
                }

                +textView {
                    textSize = 15f
                    setTextColor(config.foregroundAlpha45Color)
                    _text = detailInfo?.newest_ep?.desc ?: ""

                }..lParams(wrapContent, wrapContent) {
                    margin = config.dividerSize
                }

                +horizontalLayout {
                    views {
                        +textView {
                            textSize = 15f
                            setTextColor(config.foregroundAlpha45Color)
                            _text = "${detailInfo?.rating?.score ?: ""}分/${detailInfo?.rating?.count ?: ""}人评分"
                        }..lParams { rightMargin = config.dividerSize }

                        +textView {
                            textSize = 15f
                            setTextColor(config.foregroundAlpha45Color)
                            _text = "${NumberUtil.converString(detailInfo?.stat?.views ?: "")}次观看"
                        }
                    }
                }..lParams(wrapContent, wrapContent)
            }
        }
    }

    fun MiaoUI.descView(): View {
        val detailInfo = viewModel.detailInfo
        return verticalLayout {
            backgroundColor = config.blockBackgroundColor
            padding = config.pagePadding
            apply(ViewStyle.roundRect(dip(10)))
            views {
                +horizontalLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    views {
                        +textView {
                            text = "选择系列："
                        }
                        +spinner {
                            val mAdapter = miaoMemo(null) {
                                ArrayAdapter<String>(
                                    context,
                                    android.R.layout.simple_spinner_item
                                ).also {
                                    it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    adapter = it
                                    onItemSelectedListener = handleItemSelected
                                }
                            }
                            val seasons = viewModel.seasons
                            miaoEffect(seasons) {
                                mAdapter.clear()
                                mAdapter.addAll(seasons.map { it.title })
                            }
                            miaoEffect(viewModel.seasonsIndex) {
                                setSelection(it)
                            }
                        }..lParams(width = wrapContent)
                    }
                }..lParams {
                    bottomMargin = config.dividerSize
                }
                +textView {
                    textSize = 14f
                    _text = detailInfo?.evaluate ?: ""
                }
            }
        }
    }

    fun MiaoUI.episodesView(): View {
        return verticalLayout {
            backgroundColor = config.blockBackgroundColor
            padding = config.pagePadding
            apply(ViewStyle.roundRect(dip(10)))
            views {
                +horizontalLayout {
                    views {
                        +textView {
                            textSize = 18f
                            setTextColor(config.foregroundColor)
                            text = "剧集列表"
                        }..lParams {
                            weight = 1f
                            height = wrapContent
                            width = matchParent
                        }
                        +textView {
                            text = "更多 >"
                            setTextColor(config.themeColor)
                            textSize = 14f
                            setBackgroundResource(config.selectableItemBackgroundBorderless)
                            setOnClickListener(handleMoreClick)
                        }
                    }
                }..lParams(matchParent, wrapContent) {
                    bottomMargin = config.dividerSize
                }

                +recyclerView {
                    val lm = LinearLayoutManager(context)
                    lm.orientation = LinearLayoutManager.HORIZONTAL
                    _miaoLayoutManage(lm)

                    _miaoAdapter(
                        items = viewModel.episodes,
                        itemUi = episodeItemUi,
                        depsAry = arrayOf(playerStore.state.cid),
                    ) {
                        setOnItemClickListener(handleEpisodeItemClick)
                    }
                }..lParams {
                    width = matchParent
                }
            }
        }
    }

    fun MiaoUI.contentView(): View {
        return verticalLayout {
            views {
                +descView()..lParams(matchParent, wrapContent) {
                    bottomMargin = config.pagePadding
                }
                +episodesView()..lParams(matchParent, wrapContent) {
                    bottomMargin = config.pagePadding
                }
            }
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        val contentInsets = windowStore.getContentInsets(parentView)
        view<DoubleColumnAutofitLayout> {
            _leftPadding = contentInsets.left + config.pagePadding
            _rightPadding = contentInsets.right + config.pagePadding
            _topPadding = contentInsets.top + config.pagePadding
            _bottomPadding = contentInsets.bottom + config.pagePadding

            expandWidth = dip(620)
            leftView = headerView()
            rightView = verticalLayout {
                contentViewId = assignAndGetGeneratedId()
                dividerSize = config.dividerSize
                views {
                    +contentView()..lParams(matchParent, wrapContent)
                }
            }.wrapInNestedScrollView {
            }.wrapInSwipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                setOnRefreshListener(handleRefresh)
                _isRefreshing = viewModel.loading
            }
        }

    }

}