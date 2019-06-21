package com.a10miaomiao.bilimiao.ui.video

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
import com.a10miaomiao.bilimiao.entity.Page
import com.a10miaomiao.bilimiao.entity.bangumi.BangumiEpisode
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.bottomSheetHeaderView
import com.a10miaomiao.bilimiao.ui.player.PlayerActivity
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI

class PagesFragment : Fragment() {

    companion object {
        fun newInstance(aid: String, pages: ArrayList<Page>, index: Int): PagesFragment {
            val fragment = PagesFragment()
            val bundle = Bundle()
            bundle.putString("aid", aid)
            bundle.putParcelableArrayList("episode", pages)
            bundle.putInt("index", index)
            fragment.arguments = bundle
            return fragment
        }
    }

    val aid by lazy { arguments!!.getString("aid") }
    val pages by lazy { arguments!!.getParcelableArrayList<Page>("episode") }
    val index by lazy { arguments!!.getInt("index") }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return createUI().view
    }

    private fun createUI() = UI {
        verticalLayout {
            bottomSheetHeaderView("分P列表", View.OnClickListener {
                MainActivity.of(context)
                        .hideBottomSheet()
            })

            recyclerView {
                layoutManager = GridLayoutManager(context, 2)
                miao(pages) {
                    itemView { b ->
                        frameLayout {
                            backgroundResource = R.drawable.shape_corner
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
                                    if (index == i) {
                                        this@frameLayout.isEnabled = false
                                        textColorResource = config.themeColorResource
                                    } else {
                                        this@frameLayout.isEnabled = true
                                        textColorResource = R.color.text_black
                                    }
                                    text = item.part
                                }
                            }
                        }
                    }
                    onItemClick { item, position ->
                        VideoInfoFragment.instance
                                .palyVideo(item.cid.toString(), item.part, position)
                        MainActivity.of(context)
                                .hideBottomSheet()
                    }
                }
            }
        }
    }
}