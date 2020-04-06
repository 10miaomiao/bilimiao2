package com.a10miaomiao.bilimiao.ui.rank

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.BiliMiaoRank
import com.a10miaomiao.bilimiao.ui.bangumi.BangumiFragment
import com.a10miaomiao.bilimiao.ui.commponents.dropMenuView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout

class RankCategoryDetailsFragment : Fragment() {

    companion object {
        fun newInstance(info: BiliMiaoRank, id: Int): RankCategoryDetailsFragment {
            val fragment = RankCategoryDetailsFragment()
            val bundle = Bundle()
            bundle.putParcelable(ConstantUtil.INFO, info)
            bundle.putInt(ConstantUtil.ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val info by lazy { arguments!!.getParcelable<BiliMiaoRank>(ConstantUtil.INFO) }
    private val _id by lazy { arguments!!.getInt(ConstantUtil.ID) }
    private lateinit var viewModel: RankCategoryDetailsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { RankCategoryDetailsViewModel(context!!, info, _id) })
                .get(RankCategoryDetailsViewModel::class.java)
        return render().view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun render() = UI {
        verticalLayout {
            lparams(matchParent, matchParent)
            backgroundColor = config.windowBackgroundColor

            linearLayout {
                lparams(width = matchParent) {
                    bottomMargin = dip(5)
                }
                padding = dip(5)
                backgroundColor = config.blockBackgroundColor

                info.filter.forEachWithIndex { i1, filter ->
                    dropMenuView {
                        layoutParams = LinearLayout.LayoutParams(wrapContent, wrapContent).apply {
                            leftMargin = dip(10)
                        }
                        ico = R.drawable.ic_arrow_drop_down_24dp
                        val menu = popupMenu.menu
                        filter.values.forEachWithIndex { i2, filterItem ->
                            val item = menu.add(1, Menu.FIRST + i2, 0, filterItem.name)
                            if (viewModel.myFilter[i1].value == filterItem.value) {
                                item.isChecked = true
                                text = filterItem.name
                            }
                        }
                        menu.setGroupCheckable(1, true, true)
                        onMenuItemClick(viewModel.createMenuItemClick(i1))
                    }
                }
            }

            swipeRefreshLayout {
                setColorSchemeResources(config.themeColorResource)
                viewModel.loading.observeNotNull()(::setRefreshing)
//                viewModel.bind(viewModel::loading) { isRefreshing = it }
                setOnRefreshListener { viewModel.loadData() }
                recyclerView {
                    backgroundColor = config.blockBackgroundColor
                    if (info.type == ConstantUtil.VIDEO)
                        createVideoAdapter()
                    else if (info.type == ConstantUtil.BANGUMI)
                        createBangumiAdapter()
                }
            }

        }
    }

    private fun RecyclerView.createVideoAdapter() = miao(viewModel.videoList) {
        itemView { binding ->
            linearLayout {
                lparams(matchParent, wrapContent)
                selectableItemBackground()
                padding = dip(5)

                textView {
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    binding.bindIndexed { item, index ->
                        text = (index + 1).toString()
                        setTextColor(context.resources.getColor(
                                if (index > 2) R.color.text_black else config.themeColorResource
                        ))
                    }
                }.lparams(dip(30), wrapContent) {
                    gravity = Gravity.CENTER
                }

                rcImageView {
                    radius = dip(5)
                    binding.bind { item -> network(item.pic) }
                }.lparams(width = dip(140), height = dip(85)) {
                    rightMargin = dip(5)
                }

                verticalLayout {
                    textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textColor = config.foregroundColor
                        binding.bind { item -> text = item.title }
                    }.lparams(matchParent, matchParent) {
                        weight = 1f
                    }

                    linearLayout {
                        gravity = Gravity.CENTER_VERTICAL
                        imageView {
                            imageResource = R.drawable.icon_up
                        }.lparams {
                            width = dip(16)
                            rightMargin = dip(3)
                        }
                        textView {
                            textSize = 12f
                            textColor = config.foregroundAlpha45Color
                            binding.bind { item -> text = item.author }
                        }
                    }
                    textView {
                        textSize = 12f
                        textColor = config.foregroundAlpha45Color
                        binding.bind { item -> text = "综合评分：${item.pts}" }
                    }
                }.lparams(width = matchParent, height = matchParent)
            }
        }
        onItemClick { item, position ->
            startFragment(VideoInfoFragment.newInstance(item.aid))
        }
        layoutManager(LinearLayoutManager(context))
    }


    private fun RecyclerView.createBangumiAdapter() = miao(viewModel.bangumioList) {
        itemView { binding ->
            linearLayout {
                lparams(matchParent, wrapContent)
                selectableItemBackground()
                padding = dip(5)

                textView {
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    binding.bindIndexed { item, index ->
                        text = (index + 1).toString()
                        setTextColor(context.resources.getColor(
                                if (index > 2) R.color.text_black else config.themeColorResource
                        ))
                    }
                }.lparams(dip(30), wrapContent) {
                    gravity = Gravity.CENTER
                }

                rcImageView {
                    radius = dip(5)
                    binding.bind { item -> network(item.cover) }
                }.lparams(width = dip(100), height = dip(133)) {
                    rightMargin = dip(5)
                }

                verticalLayout {
                    textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        textSize = 16f
                        textColor = config.foregroundColor
                        binding.bind { item -> text = item.title }
                    }.lparams(matchParent, matchParent) {
                        weight = 1f
                    }

                    textView {
                        textSize = 14f
                        textColor = config.foregroundAlpha45Color
                        binding.bind { item ->
                            text = item.new_ep.index_show
                        }
                    }.lparams {
                        bottomMargin = dip(5)
                    }

                    textView {
                        textSize = 14f
                        textColor = config.foregroundAlpha45Color
                        binding.bind { item -> text = "综合评分：${item.pts}" }
                    }

                }.lparams(width = matchParent, height = matchParent)
            }
        }
        onItemClick { item, position ->
            startFragment(BangumiFragment.newInstance(item.season_id))
        }
        layoutManager(LinearLayoutManager(context))
    }

}