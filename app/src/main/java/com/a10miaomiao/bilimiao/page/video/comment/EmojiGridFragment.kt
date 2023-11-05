package com.a10miaomiao.bilimiao.page.video.comment

import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmoteInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePackagesInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePanelInfo
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.progressBar
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.config.config
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.google.android.material.card.MaterialCardView
import com.shuyu.textutillib.EmojiLayout
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.bundle.put
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.dsl.core.button
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.imageButton
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.margin
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.space
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.view
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding
import splitties.views.imageResource
import splitties.views.padding


class EmojiGridFragment : Fragment(), DIAware {

    companion object {
        fun newFragmentInstance(
            id: Int,
            type: Int,
            list: List<UserEmoteInfo>
        ): EmojiGridFragment {
            val fragment = EmojiGridFragment()
            val bundle = Bundle()
            bundle.put("id", id)
            bundle.put("type", type)
            bundle.put("list", list)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val emoteId by lazy { requireArguments().getInt("id") }
    private val emotetype by lazy { requireArguments().getInt("type") }
    private val _emotelist by lazy { requireArguments().getParcelableArrayList<UserEmoteInfo>("list") }
    private val emotelist = mutableListOf<UserEmoteInfo>()

    private var loading = false

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
        if (_emotelist.isNullOrEmpty()) {
            loadEmoteList()
        } else {
            emotelist.clear()
            emotelist.addAll(_emotelist!!)
        }
        val editText = requireActivity().findViewById<EditText>(SendCommentFragment.ID_editText)
        editText.requestFocus()
    }

    private fun loadEmoteList() = lifecycleScope.launch {
        try {
            if (emotelist.isNotEmpty()) {
                return@launch
            }
            ui.setState { loading = true }
            val res = BiliApiService.commentApi
                .emoteList(emoteId.toString())
                .awaitCall()
                .gson<ResultInfo<UserEmotePackagesInfo>>()
            if (res.isSuccess) {
                val result = res.data
                val emotePackage = result.packages.find {
                    it.id == emoteId
                }
                if(emotePackage != null) {
                    ui.setState {
                        emotelist.clear()
                        emotelist.addAll(emotePackage.emote!!)
                    }
                }
            } else {

            }
        } catch (e: Exception) {
            DebugMiao.log(e)
            e.printStackTrace()
        } finally {
            ui.setState { loading = false }
        }
    }

    private val handleBackspaceClick = View.OnClickListener {
        val editText = requireActivity().findViewById<EditText>(SendCommentFragment.ID_editText)
        val keyEvent = KeyEvent(
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_DEL
        )
        editText.dispatchKeyEvent(keyEvent);
    }

    private val handleItemClick = OnItemClickListener { adapter, view, i ->
        val item = emotelist?.getOrNull(i) ?: return@OnItemClickListener
        val editText = requireActivity().findViewById<EditText>(SendCommentFragment.ID_editText)
        val index: Int = editText.selectionStart
        val edit = editText.editableText
        if (index < 0 || index >= edit.length) {
            edit.append(item.text)
        } else {
            edit.insert(index, item.text)
        }
    }

    private val emojiItemUi = miaoBindingItemUi<UserEmoteInfo> { item, _ ->
        frameLayout {
            padding = dip(5)
            views {
                +imageView {
                    _network(item.url)
                }..lParams(dip(50), dip(50))
            }
        }
    }

    private val textEmojiItemUi = miaoBindingItemUi<UserEmoteInfo> { item, _ ->
        frameLayout {
            padding = dip(5)
            views {
                +textView {
                    _text = item.url
                    gravity = Gravity.CENTER
                }..lParams(dip(100), dip(50))
            }
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        frameLayout {

            views {
                +recyclerView {
                    tag = false
                    clipToPadding = true
                    horizontalPadding = config.pagePadding
                    val mAdapter = if (emotetype == 4) {
                        _miaoLayoutManage(
                            GridAutofitLayoutManager(requireContext(), dip(100))
                        )
                        _miaoAdapter(
                            items = emotelist,
                            itemUi = textEmojiItemUi,
                        )
                    } else {
                        _miaoLayoutManage(
                            GridAutofitLayoutManager(requireContext(), dip(60))
                        )
                        _miaoAdapter(
                            items = emotelist,
                            itemUi = emojiItemUi,
                        )
                    }
                    miaoEffect(mAdapter) {
                        it.stateRestorationPolicy =
                            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        it.setOnItemClickListener(handleItemClick)
                        it.addFooterView(space()..lParams(height = dip(50)))
                    }
                }..lParams(matchParent, matchParent)


                +view<MaterialCardView> {
                    setOnClickListener(handleBackspaceClick)
                    views {
                        +imageView {
                            imageResource = R.drawable.ic_baseline_backspace_24
                        }..lParams(dip(40), dip(30)) {
                            gravity = Gravity.CENTER
                        }
                    }

                }..lParams(dip(60), dip(40)) {
                    margin = dip(20)
                    gravity = Gravity.END or Gravity.BOTTOM
                }

                +progressBar {
                    _show = loading
                }..lParams {
                    gravity = Gravity.CENTER
                    width = dip(40)
                    height = dip(40)
                }
            }
        }

    }

}