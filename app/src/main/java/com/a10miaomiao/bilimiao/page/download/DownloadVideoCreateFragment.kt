package com.a10miaomiao.bilimiao.page.download

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.widget._textColorResource
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoPageInfo
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.template.TemplateViewModel
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.padding
import splitties.views.verticalPadding

class DownloadVideoCreateFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "创建下载任务"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<DownloadVideoCreateViewModel>(di)

    private val windowStore by instance<WindowStore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    val handleItemClick = OnItemClickListener { adapter, view, position ->
        requireActivity().toast("重新装修中")
    }

    val itemUi = miaoBindingItemUi<VideoPageInfo> { item, index ->
        frameLayout {
            setBackgroundResource(R.drawable.shape_corner)
            layoutParams = lParams {
                width = matchParent
                height = wrapContent
                horizontalMargin = dip(5)
                bottomMargin = dip(10)
            }
//            val enabled = item.cid != playerStore.state.info.cid
//            _isEnabled = enabled


            views {
                +textView {
                    horizontalPadding = dip(10)
                    verticalPadding = dip(10)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END

//                    _textColorResource = if (enabled) {
//                        R.color.text_black
//                    } else {
//                        config.themeColorResource
//                    }
                    _text = item.part

//                    b.bindIndexed { item, i ->
//                        text = item.part
//                        val index = downloadDelegate.downloadList.indexOfFirst {
//                            aid == it.avid.toString() && item.cid == it.page_data.cid.toString()
//                        }
//                        if (index != -1) {
//                            this@frameLayout.isEnabled = false
//                            this@frameLayout.backgroundResource =
//                                R.drawable.shape_corner_pressed
//                            textColor = config.lineColor
//                            return@bindIndexed
//                        }
//                        this@frameLayout.isEnabled = true
//                        val selected = viewModel.selectedList.indexOf(item.cid) != -1
//                        if (selected) {
//                            this@frameLayout.backgroundResource =
//                                R.drawable.shape_corner_pressed
//                            textColorResource = config.themeColorResource
//                        } else {
//                            this@frameLayout.backgroundResource =
//                                R.drawable.shape_corner_default
//                            textColorResource = R.color.text_black
//                        }
//                    }
                }
            }

        }
    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)
        verticalLayout {
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom

            views {
                +horizontalLayout {
                    backgroundColor = config.blockBackgroundColor
                    verticalPadding = config.pagePadding
                    _leftPadding = contentInsets.left + config.pagePadding
                    _rightPadding = contentInsets.right + config.pagePadding
                    gravity = Gravity.CENTER_VERTICAL

                    views {

                        +textView {
                            text = "选择清晰度："
                        }..lParams {
                            rightMargin = dip(5)
                        }

                        +spinner {
                            val mAdapter = miaoMemo(null) {
                                ArrayAdapter<String>(
                                    context,
                                    android.R.layout.simple_spinner_item
                                )
                            }
                            mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            adapter = mAdapter
                            miaoEffect(viewModel.acceptDescription) {
                                mAdapter.clear()
                                mAdapter.addAll(viewModel.acceptDescription)
                            }
//                    miaoEffect(viewModel.spinnerSelected) {
//                        setSelection(it)
//                    }
//                    onItemChanged(viewModel::changedSpinnerItem)
                        }..lParams(width = wrapContent)
                    }
                }

                +recyclerView {
                    backgroundColor = config.windowBackgroundColor
                    verticalPadding = config.pagePadding
                    _leftPadding = contentInsets.left
                    _rightPadding = contentInsets.right

                    _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(180))
                    )

                    _miaoAdapter(
                        items = viewModel.video.pages.toMutableList(),
                        itemUi = itemUi,
                    ) {
                        setOnItemClickListener(handleItemClick)
                    }
                }..lParams {
                    weight = 1f
                    width = matchParent
                    height = matchParent
                }

            }
        }
    }

}