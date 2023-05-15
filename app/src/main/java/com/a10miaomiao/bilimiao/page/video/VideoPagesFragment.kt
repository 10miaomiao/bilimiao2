package com.a10miaomiao.bilimiao.page.video

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.android.widget._textColorResource
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.model.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.verticalPadding

class VideoPagesFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "video.pages"
        override fun FragmentNavigatorDestinationBuilder.init() {
//            deepLink("bilimiao://video/pages")
            argument(MainNavArgs.video) {
                type = NavType.ParcelableType(VideoPagesParam::class.java)
                nullable = false
            }
            argument(MainNavArgs.index) {
                type = NavType.IntType
                defaultValue = 0
            }
        }

        fun createArguments(
            video: VideoPagesParam,
            index: Int = 0,
        ): Bundle {
            return bundleOf(
                MainNavArgs.video to video,
                MainNavArgs.index to index,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "分P列表"
    }

    override val di: DI by lazyUiDi(ui = { ui })

//    private val viewModel by diViewModel<TemplateViewModel>(di)

    private val playerStore by instance<PlayerStore>()
    private val windowStore by instance<WindowStore>()
    private val basePlayerDelegate by instance<BasePlayerDelegate>()

    private val video by lazy { requireArguments().getParcelable<VideoPagesParam>(MainNavArgs.video)!! }
    private val pages by lazy { video.pages.toMutableList() }

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
    }

    val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = pages[position]
        val playerSource = VideoPlayerSource(
            aid = video.aid,
            id = item.cid,
            coverUrl = video.pic,
            title = item.part,
            ownerId = video.ownerId,
            ownerName = video.ownerName,
        )
        basePlayerDelegate.openPlayer(playerSource)
//        Navigation.findNavController(view).popBackStack()
    }

    val itemUi = miaoBindingItemUi<VideoPagesParam.Page> { item, index ->
        frameLayout {
            setBackgroundResource(R.drawable.shape_corner)
            layoutParams = ViewGroup.MarginLayoutParams(matchParent, wrapContent).apply {
                bottomMargin = dip(10)
            }
            horizontalPadding = dip(10)
            verticalPadding = dip(10)

            val enabled = item.cid != playerStore.state.cid
            _isEnabled = enabled

            views {
                +textView {
                    horizontalPadding = dip(10)
                    verticalPadding = dip(10)
                    maxLines = 1
                    ellipsize = TextUtils.TruncateAt.END

                    _textColorResource = if (enabled) {
                        R.color.text_black
                    } else {
                        config.themeColorResource
                    }
                    _text = item.part
                }
            }

        }
    }

    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        connectStore(viewLifecycleOwner, playerStore)
        val contentInsets = windowStore.getContentInsets(parentView)

        recyclerView {
            backgroundColor = config.windowBackgroundColor
            layoutParams = ViewGroup.MarginLayoutParams(matchParent, matchParent).apply {
                _topMargin = contentInsets.top
            }
            _bottomPadding = contentInsets.bottom
            _leftPadding = contentInsets.left + config.pagePadding
            _rightPadding = contentInsets.right + config.pagePadding

            _miaoLayoutManage(
                LinearLayoutManager(requireContext())
            )

            _miaoAdapter(
                items = pages,
                itemUi = itemUi,
                depsAry = arrayOf(playerStore.state.cid)
            ) {
                setOnItemClickListener(handleItemClick)
            }
        }
    }
}