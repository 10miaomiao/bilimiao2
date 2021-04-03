package com.a10miaomiao.bilimiao.ui.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.a10miaomiao.download.BiliVideoEntry
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.delegate.DownloadDelegate
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.MiaoRecyclerViewAdapter
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import java.text.DecimalFormat


class DowmloadFragment : Fragment() {

    private var listAdapter: MiaoRecyclerViewAdapter<BiliVideoEntry>? = null
    lateinit var downloadDelegate: DownloadDelegate

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        downloadDelegate = MainActivity.of(context!!).downloadDelegate
        return MainActivity.of(context!!).dynamicTheme(this) { render().view }
    }

    private val onMenuItemClick = Toolbar.OnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
            R.id.search -> {
                startFragment(SearchFragment.newInstance())
            }
        }
        true
    }

    override fun onResume() {
        super.onResume()
        listAdapter?.notifyDataSetChanged()
    }

    private fun render() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor
            headerView {
                title("下载")
                navigationIcon(R.drawable.ic_menu_white_24dp)
                navigationOnClick {
                    MainActivity.of(context!!).openDrawer()
                }
                inflateMenu(R.menu.search)
                onMenuItemClick(onMenuItemClick)
            }

            recyclerView {
                layoutManager = LinearLayoutManager(context)
                // 取消闪烁
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                miao(downloadDelegate.downloadList) {
                    listAdapter = this
                    itemView { b ->
                        linearLayout {
                            lparams(matchParent, wrapContent)
                            selectableItemBackground()
                            padding = dip(5)

                            rcImageView {
                                radius = dip(5)
                                b.bind { item -> network(item.cover) }
                            }.lparams(width = dip(140), height = dip(85)) {
                                rightMargin = dip(5)
                            }

                            verticalLayout {
                                textView {
                                    ellipsize = TextUtils.TruncateAt.END
                                    maxLines = 2
                                    textColor = config.foregroundColor
                                    b.bind { item -> text = item.title }
                                }.lparams(matchParent, wrapContent)

                                textView {
                                    textSize = 12f
                                    textColor = config.foregroundAlpha45Color
                                    b.bind { item -> text = item.page_data.part }
                                }.lparams(matchParent, matchParent) {
                                    weight = 1f
                                }

                                textView {
                                    textSize = 12f
                                    textColor = config.foregroundAlpha45Color
                                    b.bind { item ->
                                        downloadDelegate.curVideo?.let {
                                            val curDownload = -downloadDelegate.curDownload
                                            if (curDownload != null
                                                    && item.avid == it.avid
                                                    && item.page_data.cid === it.page_data.cid) {
                                                text = curDownload.statusText
                                                return@bind
                                            }
                                        }
                                        text = if (item.is_completed) {
                                            "下载完成"
                                        } else if (item.downloaded_bytes == 0L) {
                                            "队列中"
                                        } else {
                                            val fnum = DecimalFormat("##0.00")
                                            "暂停中 ${fnum.format(item.downloaded_bytes * 1.0 / item.total_bytes * 100.0)}%"
                                        }
                                    }
                                }
                            }.lparams(width = matchParent, height = matchParent)
                        }
                    }
                    onItemClick { item, position ->
                        if (item.is_completed) {
                            // 播放视频
                            MainActivity.of(context!!)
                                    .videoPlayerDelegate
                                    .playLocalVideo(item)
                            return@onItemClick
                        }
                        toast("开始下载")
                        val downloadService = MainActivity.of(context!!)
                                .downloadDelegate
                                .downloadService
                        val curVideo = downloadDelegate.curVideo
                        if (curVideo != null) {
                            if (item.avid == curVideo?.avid
                                    && item.page_data.cid === curVideo?.page_data.cid) {
                                downloadService.stopDownload()
                                return@onItemClick
                            }
                        }
                        downloadService.startDownload(item)
                    }
                    (downloadDelegate.curDownload.observe()) {
                        val curVideo = downloadDelegate.curVideo
                        val index = downloadDelegate.downloadList.indexOfFirst {
                            it.avid == curVideo?.avid && it.page_data.cid === curVideo?.page_data.cid
                        }
                        if (index == -1) {
                            notifyDataSetChanged()
                        } else {
                            notifyItemChanged(index)
                        }
                    }
                }
            }.lparams {
                weight = 1f
            }

        }
    }
}