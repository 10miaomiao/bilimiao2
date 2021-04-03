package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.delegate.DownloadDelegate
import com.a10miaomiao.bilimiao.entity.Page
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.dropMenuView
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.time.TimeSettingFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI

class CreateDownloadFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(
                aid: String,
                bvid: String,
                title: String,
                cover: String,
                mid: Long,
                pages: ArrayList<Page>,
                index: Int
        ): CreateDownloadFragment {
            val fragment = CreateDownloadFragment()
            val bundle = Bundle()
            bundle.putString("aid", aid)
            bundle.putString("bvid", bvid)
            bundle.putString("title", title)
            bundle.putString("cover", cover)
            bundle.putLong("mid", mid)
            bundle.putParcelableArrayList("pages", pages)
            bundle.putInt("index", index)
            fragment.arguments = bundle
            return fragment
        }
    }

    val aid by lazy { arguments!!.getString("aid") }
    val bvid by lazy { arguments!!.getString("bvid") }
    val title by lazy { arguments!!.getString("title") }
    val cover by lazy { arguments!!.getString("cover") }
    val mid by lazy { arguments!!.getLong("mid") }
    val pages by lazy { arguments!!.getParcelableArrayList<Page>("pages") }
    val index by lazy { arguments!!.getInt("index") }

    lateinit var downloadDelegate: DownloadDelegate
    lateinit var viewModel: CreateDownloadViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel { CreateDownloadViewModel(context!!, aid, bvid, title, cover, mid, pages, index) }
        downloadDelegate = MainActivity.of(context!!).downloadDelegate
        return attachToSwipeBack(createUI().view)
    }

    private fun createUI() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor

            headerView {
                title("选择分P下载（测试功能）")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
            }

            linearLayout {
                backgroundColor = config.blockBackgroundColor
                padding = dip(5)
                gravity = Gravity.CENTER_VERTICAL

                textView {
                    text = "选择清晰度："
                }.lparams {
                    rightMargin = dip(5)
                }
                dropMenuView {
                    text = "清晰度"
                    ico = R.drawable.ic_arrow_drop_down_24dp

                    (viewModel.acceptDescription.observe()) {
                        popupMenu.menu.clear()
                        it.forEachIndexed { index, s ->
                            popupMenu.menu.add(s)
                            if (viewModel.acceptQuality[index] == viewModel.quality) {
                                text = s
                            }
                        }
                    }

                    onMenuItemClick {
                        val index = viewModel.acceptDescription.value.indexOf(it.title)
                        if (index != -1) {
                            viewModel.quality = viewModel.acceptQuality[index]
                        }
                    }
                }
            }.lparams(width = matchParent) { bottomMargin = dip(5) }

            recyclerView {
                layoutManager = GridLayoutManager(context, 2)
                miao(pages) {
                    itemView { b ->
                        frameLayout {
                            backgroundResource = R.drawable.shape_corner_default

                            lparams(matchParent, wrapContent) {
                                margin = dip(5)
                            }

                            textView {
                                horizontalPadding = dip(10)
                                verticalPadding = dip(10)
                                textColorResource = R.color.text_black
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                b.bindIndexed { item, i ->
                                    text = item.part
                                    val index = downloadDelegate.downloadList.indexOfFirst {
                                        aid == it.avid.toString() && item.cid == it.page_data.cid.toString()
                                    }
                                    if (index != -1) {
                                        this@frameLayout.isEnabled = false
                                        this@frameLayout.backgroundResource = R.drawable.shape_corner_pressed
                                        textColor = config.lineColor
                                        return@bindIndexed
                                    }
                                    this@frameLayout.isEnabled = true
                                    val selected = viewModel.selectedList.indexOf(item.cid) != -1
                                    if (selected) {
                                        this@frameLayout.backgroundResource = R.drawable.shape_corner_pressed
                                        textColorResource = config.themeColorResource
                                    } else {
                                        this@frameLayout.backgroundResource = R.drawable.shape_corner_default
                                        textColorResource = R.color.text_black
                                    }
                                }
                            }
                        }
                    }
                    onItemClick { item, position ->
                        val index = viewModel.selectedList.indexOf(item.cid)
                        if (index == -1) {
                            viewModel.selectedList.add(item.cid)
                        } else {
                            viewModel.selectedList.removeAt(index)
                            DebugMiao.log(viewModel.selectedList)
                        }
                    }
                    viewModel.selectedList.updateView = {
                        notifyDataSetChanged()
                    }
                }
            }.lparams {
                width = matchParent
                weight = 1f
            }

            linearLayout {
                backgroundColor = config.blockBackgroundColor
                gravity = Gravity.CENTER_VERTICAL

                textView {
                    gravity = Gravity.CENTER
                    selectableItemBackground()
                    setOnClickListener {
                        viewModel.startDownload()
                    }
                    text = "开始下载"
                }.lparams {
                    height = matchParent
                    weight = 1f
                }
            }.lparams(matchParent, dip(48))
        }
    }

}