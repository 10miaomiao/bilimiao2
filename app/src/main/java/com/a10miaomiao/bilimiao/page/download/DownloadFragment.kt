package com.a10miaomiao.bilimiao.page.download

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.download.DownloadDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerDelegate
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.DownloadStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.template.TemplateViewModel
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.toast.toast
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.padding
import java.text.DecimalFormat

class DownloadFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "我的下载"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<DownloadViewModel>(di)

    private val windowStore by instance<WindowStore>()
    private val downloadDelegate by instance<DownloadDelegate>()
    private val playerDelegate by instance<PlayerDelegate>()
    val downloadStore by instance<DownloadStore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    @OptIn(InternalCoroutinesApi::class)
    override fun onResume() {
        super.onResume()
    }

    val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = downloadDelegate.downloadList[position]
        if (item.is_completed) {
            playerDelegate.playLocalVideo(item)
            return@OnItemClickListener
        }
        toast("开始下载")
        val downloadService = downloadDelegate.downloadService
        val curVideo = viewModel.downloadStore.state.curVideo
        if (curVideo != null) {
            if (item.avid == curVideo?.avid
                && item.page_data.cid === curVideo?.page_data.cid) {
                downloadService.stopDownload()
                return@OnItemClickListener
            }
        }
        downloadService.startDownload(item)
    }

    val handleItemLongClick = OnItemLongClickListener { adapter, view, position ->
        val item = downloadDelegate.downloadList[position]
        AlertDialog.Builder(requireContext()).apply {
            setTitle("确定删除，喵？")
            setMessage("删除\"${item.title}\"?")
            setNegativeButton("确定删除") { dialog, which ->
                viewModel.delectDownload(item)
                toast("已删除了喵")
            }
            setPositiveButton("取消", null)
        }.show()
        true
    }

    val itemUi = miaoBindingItemUi<BiliVideoEntry> { item, index ->
        horizontalLayout {
            layoutParams = lParams(matchParent, wrapContent)
            setBackgroundResource(config.selectableItemBackground)
            padding = dip(5)

            views {
                +rcImageView {
                    radius = dip(5)
                    _network(item.cover)
                }..lParams(width = dip(140), height = dip(85)) {
                    rightMargin = dip(5)
                }

                +verticalLayout {
                    views {
                        +textView {
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 2
                            setTextColor(config.foregroundColor)
                            _text =  item.title
                        }..lParams(matchParent, wrapContent)

                        +textView {
                            textSize = 12f
                            setTextColor(config.foregroundAlpha45Color)
                            _text =  item.page_data.part
                        }..lParams(matchParent, matchParent) {
                            weight = 1f
                        }

                        +textView {
                            textSize = 12f
                            setTextColor(config.foregroundAlpha45Color)
                            val curVideo = viewModel.downloadStore.state.curVideo
                            val curDownload = viewModel.downloadStore.state.curDownload
                            _text = if (
                                curVideo != null
                                && curDownload != null
                                && item.avid == curVideo.avid
                                && item.page_data.cid === curVideo.page_data.cid
                            ) {
                                curDownload.statusText
                            } else if (item.is_completed) {
                                "下载完成"
                            } else if (item.downloaded_bytes == 0L) {
                                "队列中"
                            } else {
                                val fnum = DecimalFormat("##0.00")
                                "暂停中 ${fnum.format(item.downloaded_bytes * 1.0 / item.total_bytes * 100.0)}%"
                            }
                        }

                    }
                }..lParams(width = matchParent, height = matchParent)
            }
        }
    }

    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        connectStore(viewLifecycleOwner, downloadStore)
        val contentInsets = windowStore.getContentInsets(parentView)
        recyclerView {
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            // 取消闪烁
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            _miaoLayoutManage(
                LinearLayoutManager(context)
            )
            DebugMiao.log("DownloadInfo2", viewModel.downloadStore.state.curDownload)
            _miaoAdapter(
                items = downloadDelegate.downloadList,
                itemUi = itemUi
            ) {
                setOnItemClickListener(handleItemClick)
                setOnItemLongClickListener(handleItemLongClick)
//                (downloadDelegate.curDownload.observe()) {
//                    val curVideo = downloadDelegate.curVideo
//                    val index = downloadDelegate.downloadList.indexOfFirst {
//                        it.avid == curVideo?.avid && it.page_data.cid === curVideo?.page_data.cid
//                    }
//                    if (index == -1) {
//                        notifyDataSetChanged()
//                    } else {
//                        notifyItemChanged(index)
//                    }
//                }
            }
        }
    }

}