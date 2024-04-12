package com.a10miaomiao.bilimiao.page.video.comment

import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.a10miaomiao.miao.binding.android.view._backgroundColor
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._tag
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.attr
import com.a10miaomiao.bilimiao.comm.delegate.helper.SupportHelper
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePackageInfo
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.miaoStore
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.commponents.comment.videoCommentView
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.rcImageView
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.resources.appStyledInt
import splitties.resources.styledInt
import splitties.views.backgroundColor
import splitties.views.dsl.core.button
import splitties.views.dsl.core.editText
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.horizontalMargin
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.margin
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.verticalMargin
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.padding


class SendCommentFragment : Fragment(), DIAware, MyPage, ViewTreeObserver.OnGlobalLayoutListener {

    companion object : FragmentNavigatorBuilder() {
        override val name = "video.comment.send"

        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.params) {
                type = NavType.ParcelableType(SendCommentParam::class.java)
                nullable = false
            }
        }

        fun createArguments(
            params: SendCommentParam
        ): Bundle {
            return bundleOf(
                MainNavArgs.params to params,
            )
        }

        internal val ID_editText = View.generateViewId()
        private val ID_emojiGrid = View.generateViewId()
    }

    override val pageConfig = myPageConfig {
        title = "发布评论"
        menus = listOf(
            MenuItemPropInfo(
                key = MenuKeys.send,
                iconResource = R.drawable.ic_baseline_send_24,
                title = "发布"
            ),
            if (viewModel.isShowEmoteGrid) {
                MenuItemPropInfo(
                    key = MenuKeys.keyboard,
                    iconResource = R.drawable.ic_baseline_keyboard_24,
                    title = "键盘"
                )
            } else {
                MenuItemPropInfo(
                    key = MenuKeys.emoji,
                    iconResource = R.drawable.ic_baseline_emoji_emotions_24,
                    title = "表情"
                )
            }

        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            MenuKeys.send -> {
                val editText = requireActivity().findViewById<EditText?>(ID_editText)
                val message = editText.text.toString()
                viewModel.sendComment(message)
            }
            MenuKeys.keyboard -> {
                showSoftInput()
                viewModel.setEmoteGridDisplay(false)
            }
            MenuKeys.emoji -> {
                hideSoftInput()
                viewModel.setEmoteGridDisplay(true)
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val supportHelper by instance<SupportHelper>()

    private val viewModel by diViewModel<SendCommentViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val fMap = mutableMapOf<Int, Fragment>()
            viewModel.currentEmotePackage.collect {
                val emotePackage = it ?: return@collect
                val f = fMap.getOrElse(emotePackage.id) {
                    EmojiGridFragment.newFragmentInstance(
                        emotePackage.id,
                        emotePackage.type,
                        emotePackage.emote ?: emptyList(),
                    ).also { fMap[emotePackage.id] = it }
                }
                childFragmentManager.commit {
                    replace(ID_emojiGrid, f)
                }
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.isShowEmoteGrid) {
                        viewModel.setEmoteGridDisplay(false)
                    } else if (viewModel.isShowSoftInput) {
                        hideSoftInput()
                        viewModel.setSoftInputDisplay(false)
                    } else {
                        findNavController().popBackStack()
                    }
                }
            })
    }

    override fun onStart() {
        super.onStart()
        val editText = requireActivity().findViewById<EditText?>(ID_editText)
        editText.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onStop() {
        super.onStop()
        val editText = requireActivity().findViewById<EditText?>(ID_editText)
        editText.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        val editText = requireActivity().findViewById<EditText?>(ID_editText)
        val r = Rect().also(editText::getWindowVisibleDisplayFrame)
        val screenHeight = editText.rootView.height
        val heightDifference = screenHeight - r.bottom
        if (heightDifference > requireContext().dip(200)) {
            viewModel.setEmoteGridDisplay(false)
            viewModel.setSoftInputDisplay(true)
        } else {
            viewModel.setSoftInputDisplay(false)
        }
    }

    private fun showSoftInput() {
        requireActivity().findViewById<EditText?>(ID_editText)?.let(
            supportHelper::showSoftInput
        )
    }

    private fun hideSoftInput() {
        requireActivity().findViewById<EditText?>(ID_editText)?.let(
            supportHelper::hideSoftInput
        )
//        val editText = requireActivity().findViewById<EditText?>(ID_editText)
//        supportHelper.hideSoftInput(editText)
    }

    private val handleEmoteBtnClick = View.OnClickListener {
        viewModel.setEmoteGridDisplay(!viewModel.isShowEmoteGrid)
    }

    private val handleEmotePackageItemClick = OnItemClickListener { adapter, view, i ->
        val selectPackage = viewModel.emotePackageList.getOrNull(i) ?: return@OnItemClickListener
        viewModel.setSelectPackage(selectPackage)
        adapter.notifyDataSetChanged()
    }

    private val emotePackageItemUi = miaoBindingItemUi<UserEmotePackageInfo> { item, _ ->
        frameLayout {
            _backgroundColor = if (item.id == viewModel.currentEmotePackage.value?.id) {
                config.windowBackgroundColor
            } else {
                config.blockBackgroundColor
            }
            padding = dip(10)
            views {
                +imageView {
                    _network(item.url)
                }..lParams(dip(40), dip(40))
            }
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        miaoEffect(viewModel.isShowEmoteGrid) {
            pageConfig.notifyConfigChanged()
        }

        verticalLayout {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right
            _topPadding = contentInsets.top + config.pagePadding
            _bottomPadding = contentInsets.bottom + windowStore.bottomAppBarHeight

            views {
                +view<TextInputLayout>(
//                    theme = context.attr(R.attr.textInputFilledStyle)
                ) {
                    hint = "请输入评论"

                    views {
                        +view<TextInputEditText>(ID_editText) {
                            gravity = Gravity.TOP
                        }..lParams(matchParent, matchParent)
                    }
                }..lParams(matchParent, matchParent) {
                    weight = 1f
                    horizontalMargin = config.pagePadding
                    bottomMargin = config.smallPadding
                }

                +view<MaterialCardView> {
                    radius = dip(5f)
                    _show = !viewModel.isShowSoftInput && !viewModel.isShowEmoteGrid
                    views {
                        val params = viewModel.params
                        when (viewModel.params.type) {
                            // 普通视频
                            1 -> {
                                +videoItem(
                                    title = params.title,
                                    pic = params.image,
                                    upperName = params.name,
                                    remark = "在此视频下发布评论"
                                )
                            }
                            // 一级评论
                            2, 3 -> {
                                +verticalLayout {
                                    padding = dip(10)

                                    views {
                                        +textView {
                                            text = "回复给此条评论"
                                            setTextColor(config.foregroundAlpha45Color)
                                        }..lParams {
                                            bottomMargin = dip(5)
                                        }
                                        +horizontalLayout {
                                            views {
                                                +rcImageView {
                                                    isCircle = true
                                                    _network(params.image)
                                                }..lParams {
                                                    width = dip(40)
                                                    height = dip(40)
                                                    rightMargin = dip(10)
                                                    topMargin = dip(4)
                                                }

                                                +verticalLayout {
                                                    views {
                                                        +textView {
                                                            setTextColor(config.foregroundColor)
                                                            textSize = 14f
                                                            _text = params.name
                                                        }

                                                        +textView {
                                                            setTextColor(config.foregroundColor)
                                                            textSize = 16f
                                                            _text = params.content

                                                            ellipsize = TextUtils.TruncateAt.END
                                                            maxLines = 3
                                                        }..lParams {
                                                            width = matchParent
                                                            height = wrapContent
                                                            verticalMargin = dip(5)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }..lParams(matchParent, wrapContent) {
                    horizontalMargin = config.pagePadding
                    verticalMargin = config.smallPadding
                }

                +frameLayout(ID_emojiGrid) {
                    _show = viewModel.isShowEmoteGrid
                }..lParams(matchParent, dip(250))
                +recyclerView {
                    _show = viewModel.isShowEmoteGrid
                    horizontalPadding = config.pagePadding
                    backgroundColor = config.blockBackgroundColor

                    _miaoLayoutManage(
                        LinearLayoutManager(requireContext()).also {
                            it.orientation = LinearLayoutManager.HORIZONTAL
                        }
                    )
                    _miaoAdapter(
                        items = viewModel.emotePackageList,
                        itemUi = emotePackageItemUi,
                    ) {
                        stateRestorationPolicy =
                            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        setOnItemClickListener(handleEmotePackageItemClick)
                    }
                }..lParams(matchParent, dip(60))
            }
        }
    }

}