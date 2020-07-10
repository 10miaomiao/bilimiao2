package com.a10miaomiao.bilimiao.ui.bangumi

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.bangumi.BangumiEpisode
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.bottomSheetHeaderView
import com.a10miaomiao.bilimiao.ui.player.PlayerActivity
import com.a10miaomiao.bilimiao.utils.newViewModelFactory
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView

class EpisodesFragment : Fragment() {

    companion object {
        fun newInstance(episode: ArrayList<BangumiEpisode>): EpisodesFragment {
            val fragment = EpisodesFragment()
            val bundle = Bundle()
            bundle.putParcelableArrayList("episode", episode)
            fragment.arguments = bundle
            return fragment
        }
    }

    val episode by lazy { arguments!!.getParcelableArrayList<BangumiEpisode>("episode") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return createUI().view
    }

    private fun createUI() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor

            bottomSheetHeaderView("剧集列表", View.OnClickListener {
                MainActivity.of(context)
                        .hideBottomSheet()
            })

            recyclerView {
                layoutManager = GridLayoutManager(context, 2)
                miao(episode) {
                    itemView { b ->
                        verticalLayout {
                            backgroundResource = R.drawable.shape_corner
                            lparams(matchParent, wrapContent) {
                                margin = dip(5)
                            }
                            horizontalPadding = dip(10)
                            verticalPadding = dip(10)
                            gravity = Gravity.LEFT

                            textView {
                                textColorResource = R.color.text_black
                                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                                b.bind { item -> text = "第${item.index}集" }
                            }.lparams {
                                bottomMargin = dip(5)
                            }

                            textView {
                                textColorResource = R.color.text_black
                                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                gravity = Gravity.LEFT
                                textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                                b.bind { item -> text = item.index_title }
                            }.lparams()
                        }
                    }
                    onItemClick { item, position ->
                        MainActivity.of(context!!).videoPlayerDelegate.playBangumi(
                                item.section_id,
                                item.ep_id,
                                item.cid.toString(),
                                item.index_title
                        )

                    }
                }
            }
        }
    }
}