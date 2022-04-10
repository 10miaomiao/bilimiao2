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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.widget._textColorResource
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.bangumi.EpisodeInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.layout.DoubleColumnAutofitLayout
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.*
import splitties.views.dsl.constraintlayout.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class BangumiDetailFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "番剧详情"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<BangumiDetailViewModel>(di)

    private val windowStore: WindowStore by instance()

    private val playerDelegate: PlayerDelegate by instance()

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
        playerDelegate.playBangumi(
            item.section_id,
            item.ep_id,
            item.cid.toString(),
            item.index_title
        )
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
            _isEnabled = !isSelect

            views {
                +textView {
                    textColorResource = R.color.text_black
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    _text = "第${item.index}集"
                    _textColorResource = if (isSelect) {
                        config.themeColorResource
                    } else {
                        R.color.text_black
                    }
                }..lParams {
                    bottomMargin = dip(5)
                }

                +textView {
                    textColorResource = R.color.text_black
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    maxWidth = dip(200)
                    minWidth = dip(100)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END
                    gravity = Gravity.LEFT
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START

                    _text = item.index_title
                    _textColorResource = if (isSelect) {
                        config.themeColorResource
                    } else {
                        R.color.text_black
                    }
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
                +textView {
                    textSize = 18f
                    setTextColor(config.foregroundColor)
                    text = "剧集列表"
                }..lParams {
                    bottomMargin = config.dividerSize
                }
                +recyclerView {
                    val lm = LinearLayoutManager(context)
                    lm.orientation = LinearLayoutManager.HORIZONTAL
                    _miaoLayoutManage(lm)

                    _miaoAdapter(
                        items = viewModel.episodes,
                        itemUi = episodeItemUi,
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