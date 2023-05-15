package com.a10miaomiao.bilimiao.page.download

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.download.DownloadDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.model.LocalVideoPlayerSource
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.DownloadStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.listener.OnItemLongClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.InternalCoroutinesApi
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.toast.toast
import splitties.views.dsl.core.*
import splitties.views.dsl.core.lParams
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.gravityCenter
import splitties.views.padding
import java.text.DecimalFormat

class DownloadFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "download"
    }

    override val pageConfig = myPageConfig {
        title = "我的下载"
        menus = listOf(
            MenuItemPropInfo(
                key = 0,
                title = "提示",
                iconResource = R.drawable.ic_baseline_lightbulb_24
            )
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        when (menuItem.key) {
            0 -> {
                showHelpDialog()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<DownloadViewModel>(di)

    private val windowStore by instance<WindowStore>()
    private val downloadDelegate by instance<DownloadDelegate>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()
    val downloadStore by instance<DownloadStore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            val downloadPath = downloadDelegate.downloadService.getDownloadPath()
            setTitle("提示")
            setMessage("下载的文件保存在：\n$downloadPath\n目录结构与B站官方客户端保持一致，可与B站官方客户端相互复制缓存文件")
            setNegativeButton("复制路径") { dialog, which ->
                copyDownloadPath()
            }
            setPositiveButton("取消", null)
        }.show()
    }

    private fun copyDownloadPath() {
        val downloadPath = downloadDelegate.downloadService.getDownloadPath()
        val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("path", downloadPath)
        clipboardManager.setPrimaryClip(clipData)
        toast("下载目录路径已复制到剪切板")
    }

    val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = downloadDelegate.downloadList[position]
        if (item.is_completed) {
            val playerSorce = LocalVideoPlayerSource(requireActivity(), item)
            basePlayerDelegate.openPlayer(playerSorce)
            return@OnItemClickListener
        }
        toast("开始下载")
        val downloadService = downloadDelegate.downloadService
        val curVideo = viewModel.downloadStore.state.curVideo
        if (curVideo != null) {
            if (item.avid == curVideo?.avid
                && item.page_data.cid === curVideo?.page_data?.cid) {
                downloadService.stopDownload()
                return@OnItemClickListener
            }
        }
        downloadService.startDownload(item)
    }

    val handleItemLongClick = OnItemLongClickListener { adapter, view, position ->
        val item = downloadDelegate.downloadList[position]
        MaterialAlertDialogBuilder(requireContext()).apply {
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
        verticalLayout {
            views {
                +recyclerView {
                    _leftPadding = contentInsets.left
                    _rightPadding = contentInsets.right

                    // 取消闪烁
                    (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                    _miaoLayoutManage(
                        LinearLayoutManager(context)
                    )
                    val downloadState = viewModel.downloadStore.state
                    val mAdapter = _miaoAdapter(
                        items = downloadDelegate.downloadList,
                        itemUi = itemUi,
                        depsAry = arrayOf(downloadState)
                    ) {
                        setOnItemClickListener(handleItemClick)
                        setOnItemLongClickListener(handleItemLongClick)
                    }
                    headerViews(mAdapter) {
                        +frameLayout {
                            _topPadding = contentInsets.top
                        }
                    }
                    footerViews(mAdapter) {
                        +frameLayout {
                            _topPadding = contentInsets.bottom
                        }
                    }
                }..lParams(matchParent, matchParent) {
                    weight = 1f
                }
            }
        }

    }

}