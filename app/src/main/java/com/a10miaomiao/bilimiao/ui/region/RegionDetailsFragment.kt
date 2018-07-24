package com.a10miaomiao.bilimiao.ui.region

import android.content.Context
import android.graphics.Color
import android.graphics.ImageFormat
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.base.BaseFragment
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.Home
import com.a10miaomiao.bilimiao.entity.RegionTypeDetailsInfo
import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.dropMenuView
import com.a10miaomiao.bilimiao.ui.commponents.loadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.model.DateModel
import com.a10miaomiao.bilimiao.ui.commponents.rcLayout
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.MiaoAnkoContext
import com.a10miaomiao.miaoandriod.MiaoFragment
import com.a10miaomiao.miaoandriod.MiaoUI
import com.a10miaomiao.miaoandriod.adapter.MiaoList
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.bind
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class RegionDetailsFragment : MiaoFragment() {

    val tid by lazy { arguments!!.getInt(ConstantUtil.TID) }
    val timeFrom = DateModel(binding)
    val timeTo = DateModel(binding)

    private var pageNum = 1
    private val pageSize = 10
    private var rankOrder = "click"  //排行依据

    private var loading by binding.miao(false)
    private var loadState by binding.miao(LoadMoreView.State.LOADING)

    private var list = MiaoList<RegionTypeDetailsInfo.Result>()
    private var subscriber: Disposable? = null

    override fun initView() {
        timeFrom.read(context!!, ConstantUtil.TIME_FROM)
        timeTo.read(context!!, ConstantUtil.TIME_TO)
        subscriber = RxBus.getInstance().on(ConstantUtil.TIME_CHANGE) {
            val timeFrom = DateModel(binding).read(context!!, ConstantUtil.TIME_FROM)
            val timeTo = DateModel(binding).read(context!!, ConstantUtil.TIME_TO)
            if (this.timeFrom.diff(timeFrom) || this.timeTo.diff(timeTo)) {
                this.timeFrom.setValue(timeFrom)
                this.timeTo.setValue(timeTo)
                refreshList()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        subscriber?.dispose()
    }

    override fun loadData() {
        if (list.size >= pageNum * pageSize)
            return
        if (loadState == LoadMoreView.State.NOMORE) {
            return
        }
        loading = true
        val url = BiliApiService.getRegionTypeVideoList(tid, rankOrder, pageNum, pageSize, timeFrom.getValue(), timeTo.getValue())
        MiaoHttp.getJson(url, RegionTypeDetailsInfo::class.java)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    loading = false
                    list.addAll(data.result)
                    if (data.result.size < pageSize) {
                        loadState = LoadMoreView.State.NOMORE
                    }
                }, { err ->
                    loading = false
                    loadState = LoadMoreView.State.FAIL
                    err.printStackTrace()
                })
    }

    private fun refreshList() {
        pageNum = 1
        list.clear()
        loadState = LoadMoreView.State.LOADING
        loadData()
    }

    // arrayOf("click", "scores", "stow", "coin", "dm")
    private fun getRankOrder(@IdRes id: Int) = when (id) {
        R.id.order_click -> "click"
        R.id.order_scores -> "scores"
        R.id.order_stow -> "stow"
        R.id.order_click -> "coin"
        R.id.order_dm -> "dm"
        else -> "click"
    }

    override fun render() = MiaoUI {
        verticalLayout {
            linearLayout {
                backgroundColor = Color.WHITE
                padding = dip(5)
                gravity = Gravity.CENTER_VERTICAL
                textView {
                    bind { text = timeFrom.getValue("-") + " 至 " + timeTo.getValue("-") }
                    textSize = 14f
                    setOnClickListener {
                        RxBus.getInstance().send(ConstantUtil.START_FRAGMENT, TimeSettingFragment())
                    }
                }.lparams(width = matchParent, weight = 1f)
                dropMenuView {
                    text = "播放量"
                    ico = R.drawable.ic_arrow_drop_down_24dp
                    popupMenu.inflate(R.menu.rank_order)
                    onMenuItemClick {
                        val rankOrder =getRankOrder(it.itemId)
                        if (rankOrder != this@RegionDetailsFragment.rankOrder){
                            this@RegionDetailsFragment.rankOrder = rankOrder
                            refreshList()
                        }
                    }
                }
            }.lparams(width = matchParent) { bottomMargin = dip(5) }
            swipeRefreshLayout {
                setColorSchemeResources(R.color.colorPrimary)
                bind(::loading) { isRefreshing = it }
                setOnRefreshListener { refreshList() }
                recyclerView {
                    backgroundColor = Color.WHITE
                }.miao(list).itemView { binding ->
                    linearLayout {
                        lparams(matchParent, wrapContent)
                        selectableItemBackground()
                        padding = dip(5)

                        rcLayout {
                            roundCorner = dip(5)
                            imageView {
                                // scaleType = ImageView.ScaleType.CENTER
                                binding.bind { item -> src(item.pic) }
                            }.lparams(matchParent, matchParent)
                        }.lparams(width = dip(140), height = dip(85)) {
                            rightMargin = dip(5)
                        }

                        verticalLayout {
                            textView {
                                ellipsize = TextUtils.TruncateAt.END
                                maxLines = 2
                                textColorResource = R.color.colorForeground
                                binding.bind { item -> text = item.title }
                            }.lparams(matchParent, matchParent) {
                                weight = 1f
                            }

                            linearLayout {
                                gravity = Gravity.CENTER_VERTICAL
                                imageView {
                                    imageResource = R.drawable.icon_up
                                }.lparams {
                                    width = dip(16)
                                    rightMargin = dip(3)
                                }
                                textView {
                                    textSize = 12f
                                    textColorResource = R.color.black_alpha_45
                                    binding.bind { item -> text = item.author }
                                }
                            }

                            linearLayout {
                                gravity = Gravity.CENTER_VERTICAL
                                imageView {
                                    imageResource = R.drawable.ic_play_circle_outline_black_24dp
                                }.lparams {
                                    width = dip(16)
                                    rightMargin = dip(3)
                                }
                                textView {
                                    textSize = 12f
                                    textColorResource = R.color.black_alpha_45
                                    binding.bind { item -> text = NumberUtil.converString(item.play) }
                                }
                                space().lparams(width = dip(10))
                                imageView {
                                    imageResource = R.drawable.ic_subtitles_black_24dp
                                }.lparams {
                                    width = dip(16)
                                    rightMargin = dip(3)
                                }
                                textView {
                                    textSize = 12f
                                    textColorResource = R.color.black_alpha_45
                                    binding.bind { item -> text = NumberUtil.converString(item.video_review) }
                                }
                            }

                            linearLayout {

                            }
                        }.lparams(width = matchParent, height = matchParent)
                    }
                }.layoutManager(LinearLayoutManager(activity)).onItemClick { item, position ->
                    IntentHandlerUtil.openWithPlayer(activity!!, IntentHandlerUtil.TYPE_VIDEO, item.id)
                }.addFootView {
                    loadMoreView {
                        layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
                        bind(::loadState) { state = it }
                    }
                }.onLoadMore {
                    pageNum++
                    loadData()
                }
            }
        }
    }

    companion object {
        fun newInstance(tid: Int): RegionDetailsFragment {
            val fragment = RegionDetailsFragment()
            val bundle = Bundle()
            bundle.putInt(ConstantUtil.TID, tid)
            fragment.arguments = bundle
            return fragment
        }
    }
}