package com.a10miaomiao.bilimiao.page.download

import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.widget._textColorResource
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.*
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class DownloadVideoCreateFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "download.add"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.video) {
                type = NavType.ParcelableType(DownloadVideoCreateParam::class.java)
            }
        }

        fun createArguments(video: DownloadVideoCreateParam): Bundle {
            return bundleOf(
                MainNavArgs.video to video
            )
        }
    }

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
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    val handleConfirmClick = View.OnClickListener {
        viewModel.startDownload()
    }

    val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.video.pages[position]
        viewModel.selectedItem(item)
    }

    val handleItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            viewModel.selectedQuality(viewModel.acceptQuality[position])
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }

    val itemUi = miaoBindingItemUi<DownloadVideoCreateParam.Page> { item, index ->
        frameLayout {
            setBackgroundResource(R.drawable.shape_corner)
            layoutParams = lParams {
                width = matchParent
                height = wrapContent
                horizontalMargin = dip(5)
                bottomMargin = dip(10)
            }

            val isCreated = viewModel.downloadedList.indexOfFirst {
                item.cid == it.page_data?.cid?.toString()
            } != -1
            val isSelected = viewModel.selectedList.indexOf(item.cid) != -1
            _isEnabled = !isCreated
            _backgroundResource = if (isCreated || isSelected) {
                R.drawable.shape_corner_pressed
            } else {
                R.drawable.shape_corner_default
            }

            views {
                +textView {
                    horizontalPadding = dip(10)
                    verticalPadding = dip(10)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END

                    _text = item.part
                    _textColorResource = if (isCreated || isSelected) {
                        config.themeColorResource
                    } else {
                        R.color.text_black
                    }
                }
            }

        }
    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)
        verticalLayout {

            views {
                +horizontalLayout {
                    backgroundColor = config.blockBackgroundColor
                    gravity = Gravity.CENTER_VERTICAL

                    bottomPadding = config.pagePadding
                    _topPadding = contentInsets.top + config.pagePadding
                    _leftPadding = contentInsets.left + config.pagePadding
                    _rightPadding = contentInsets.right + config.pagePadding

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
                            miaoEffect(viewModel.qualityIndex) {
                                setSelection(it)
                            }
                            onItemSelectedListener = handleItemSelectedListener
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
                        isForceUpdate = true,
                    ) {
                        setOnItemClickListener(handleItemClick)
                    }
                }..lParams {
                    weight = 1f
                    width = matchParent
                    height = matchParent
                }

                +frameLayout {
                    _bottomPadding = contentInsets.bottom + config.dividerSize
                    topPadding = config.dividerSize
                    horizontalPadding = config.pagePadding

                    setBackgroundColor(config.blockBackgroundColor)

                    views {
                        +frameLayout {
                            setBackgroundColor(config.windowBackgroundColor)
                            apply(ViewStyle.roundRect(dip(24)))
                            setOnClickListener(handleConfirmClick)

                            views {
                                +textView{
                                    setBackgroundResource(config.selectableItemBackground)
                                    gravity = Gravity.CENTER
                                    text = "开始下载"
                                    setTextColor(config.foregroundAlpha45Color)
                                    gravity = Gravity.CENTER
                                }
                            }

                        }..lParams {
                            width = matchParent
                            height = dip(48)
                        }
                    }
                }
            }
        }
    }

}