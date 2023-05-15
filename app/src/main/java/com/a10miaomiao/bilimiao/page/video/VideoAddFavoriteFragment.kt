package com.a10miaomiao.bilimiao.page.video

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._isChecked
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.footerViews
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.toast.toast
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.padding
import splitties.views.rightPadding

class VideoAddFavoriteFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "video.add_fav"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
        }

        fun createArguments(
            id: String,
        ): Bundle {
            return bundleOf(
                MainNavArgs.id to id,
            )
        }
    }


    override val pageConfig = myPageConfig {
        title = "选择收藏夹"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<VideoAddFavoriteViewModel>(di)

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

    private fun addFavorite (view: View) {
        val favIds = viewModel.list
            .filter { it.fav_state == 1 }
            .map { it.id }
        val addIds = viewModel.list
            .filter { it.fav_state != 1 && (viewModel.selectedMap[it.id] ?: false) }
            .map { it.id }
        val delIds = viewModel.list
            .filter { it.fav_state == 1 && !(viewModel.selectedMap[it.id] ?: true) }
            .map { it.id }
        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val fragment = navHostFragment.childFragmentManager.primaryNavigationFragment
        if (fragment is VideoInfoFragment) {
            fragment.confirmFavorite(
                favIds = favIds,
                addIds = addIds,
                delIds = delIds,
            )
        }
        viewModel.selectedMap.clear()
        Navigation.findNavController(view).popBackStack()
    }

    private val handleConfirmClick = View.OnClickListener {
        if (
            viewModel.list.find {
                if (it.fav_state == 1) {
                    !(viewModel.selectedMap[it.id] ?: true)
                } else {
                    viewModel.selectedMap[it.id] ?: false
                }
            } != null
        ) {
            addFavorite(it)
        } else {
            toast("请选择收藏夹")
        }
    }

    private val handleCheckedChange = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val tag = buttonView.tag
        if (tag is String) {
            viewModel.selectedMap[tag] = isChecked
        }
    }

    val itemUi = miaoBindingItemUi<MediaListInfo> { item, index ->
        horizontalLayout {
            padding = config.dividerSize
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(config.selectableItemBackground)
            layoutParams = lParams(matchParent, wrapContent)

            views {
                +verticalLayout {
                    horizontalPadding = config.dividerSize
                    views {
                        +textView {
                            textSize = 18f
                            setTextColor(config.foregroundColor)
                            _text = item.title
                        }
                        +textView {
                            setTextColor(config.foregroundAlpha45Color)
                            _text = "${item.media_count}个内容"
                        }
                    }

                }..lParams { weight = 1f }

                +checkBox {
                    rightPadding = config.dividerSize
                    _tag = item.id
                    _isChecked = item.fav_state == 1

                    setOnCheckedChangeListener(handleCheckedChange)
                }
            }

        }
    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)

        verticalLayout {
            setBackgroundColor(config.blockBackgroundColor)
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            views {
                +recyclerView {
                    _miaoLayoutManage(LinearLayoutManager(requireContext()))
                    val mAdapter = _miaoAdapter(
                        items = viewModel.list,
                        itemUi = itemUi,
                    )
                    footerViews(mAdapter) {
                        +progressBar {
                            _show = viewModel.loading
                        }..lParams {
                            topMargin = dip(50)
                            width = dip(64)
                            height = dip(64)
                            gravity = Gravity.CENTER
                        }
                    }
                }..lParams(matchParent, matchParent) {
                    weight = 1f
                }
                +frameLayout {
                    setBackgroundColor(config.windowBackgroundColor)
                    apply(ViewStyle.roundRect(dip(24)))
                    setOnClickListener(handleConfirmClick)

                    views {
                        +textView{
                            setBackgroundResource(config.selectableItemBackground)
                            gravity = Gravity.CENTER
                            text = "完成"
                            setTextColor(config.foregroundAlpha45Color)
                            gravity = Gravity.CENTER
                        }
                    }

                }..lParams {
                    width = matchParent
                    height = dip(48)
                    topMargin = dip(10)
                    bottomMargin = dip(20)
                    horizontalMargin = dip(20)
                }
            }
        }
    }

}