package com.a10miaomiao.bilimiao.page.search.result

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.widget._textColor
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler.*
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.search.SearchResultFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.bumptech.glide.Glide
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.verticalPadding

class VideoRegionFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "search.video.region"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
        }

        fun createArguments(id: String): Bundle {
            return bundleOf(
                MainNavArgs.id to id
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "选择视频分区"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<VideoRegionViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    val handleItemClick = OnItemClickListener {  adapter, view, position ->
        val navHostFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val fragment = navHostFragment.childFragmentManager.primaryNavigationFragment
        if (fragment is SearchResultFragment) {
            val region = viewModel.regionList[position]
            fragment.changeVideoRegion(region)
        }
        val nav = findNavController()
        nav.popBackStack()
    }

    val itemUi = miaoBindingItemUi<RegionInfo> { item, index ->
        shadowLayout {
            val isCheck = item.tid.toString() == viewModel.checkRegionId
            setStrokeColorTrue(config.themeColor)
            setStrokeWidth(dip(1))
            miaoEffect(isCheck) {
                if (it) {
                    setStrokeColor(config.themeColor)
                    isEnabled = false
                } else {
                    setStrokeColor(config.blockBackgroundColor)
                    isEnabled = true
                }
            }
            setLayoutBackground(config.blockBackgroundColor)
            setCornerRadius(dip(10))

            layoutParams = lParams(matchParent, wrapContent) {
                margin = config.dividerSize
            }
            views {
                +horizontalLayout {
                    setBackgroundResource(config.selectableItemBackground)
                    gravity = Gravity.CENTER
                    verticalPadding = dip(10)

                    views {
                        +imageView {
                            miaoEffect(listOf(item.icon, item.logo)) {
                                if (item.icon != null) {
                                    Glide.with(context)
                                        .load(item.icon)
                                        .override(dip(24), dip(24))
                                        .into(this)
                                    visibility = View.VISIBLE
                                } else if (item.logo != null) {
                                    Glide.with(context)
                                        .loadImageUrl(item.logo!!)
                                        .override(dip(24), dip(24))
                                        .into(this)
                                    visibility = View.VISIBLE
                                } else {
                                    visibility = View.GONE
                                }
                            }
                        }..lParams(dip(24), dip(24)) {
                            rightMargin = config.dividerSize
                        }

                        +textView {
                            _text = item.name
                            gravity = Gravity.CENTER
                            _textColor = if (isCheck) {
                                config.themeColor
                            } else {
                                config.foregroundAlpha45Color
                            }
                        }
                    }
                }..lParams(matchParent, matchParent)
            }
        }
    }

    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, viewModel.regionStore)
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        recyclerView {
            _leftPadding = contentInsets.left + config.pagePadding
            _rightPadding = contentInsets.right + config.pagePadding
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom
            backgroundColor = config.windowBackgroundColor

            _miaoLayoutManage(
                GridAutofitLayoutManager(requireContext(), requireContext().dip(100))
            )

            _miaoAdapter(
                items = viewModel.regionList,
                itemUi = itemUi,
                depsAry = arrayOf(viewModel.checkRegionId)
            ) {
                setOnItemClickListener(handleItemClick)
            }
        }
    }

}