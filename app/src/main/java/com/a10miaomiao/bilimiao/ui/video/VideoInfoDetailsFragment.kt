package com.a10miaomiao.bilimiao.ui.video

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.mySpannableTextView
import com.a10miaomiao.bilimiao.ui.commponents.rcImageView
import com.a10miaomiao.bilimiao.ui.upper.UpperInfoFragment
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView

class VideoInfoDetailsFragment : Fragment() {

    companion object {
        fun newInstance(id: String): VideoInfoDetailsFragment {
            val fragment = VideoInfoDetailsFragment()
            val bundle = Bundle()
            bundle.putString(ConstantUtil.ID, id)
            fragment.arguments = bundle
            return fragment
        }
    }

    val id by lazy { arguments!!.getString(ConstantUtil.ID) }
    private lateinit var viewModel: VideoInfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProviders.of(VideoInfoFragment.instance, newViewModelFactory { VideoInfoViewModel(context!!, id) })
                .get(VideoInfoViewModel::class.java)
        return createUI().view
    }

    private fun createUI() = UI {
        val observeInfo = viewModel.info.observeNotNull()
        nestedScrollView {
            verticalLayout {
                lparams {
                    width = matchParent
                    height = matchParent
                    verticalPadding = dip(10)
                }
                // 标题
                textView {
                    textSize = 16f
                    ellipsize = TextUtils.TruncateAt.END
                    maxLines = 2
                    textColorResource = R.color.colorForeground
                    observeInfo {
                        text = it!!.title
                    }
                }.lparams {
                    horizontalMargin = dip(10)
                }

                // 视频信息
                linearLayout {
                    lparams {
                        width = matchParent
                        height = wrapContent
                        verticalMargin = dip(5)
                        horizontalMargin = dip(10)
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    imageView {
                        imageTintList = ColorStateList.valueOf(config.blackAlpha45)
                        setImageResource(R.drawable.ic_info_views)
                    }.lparams(dip(14), dip(14)) {
                        gravity = Gravity.CENTER
                    }
                    textView {
                        textSize = 12f
                        textColor = config.blackAlpha45
                        observeInfo {
                            text = NumberUtil.converString(it!!.stat.view)
                        }
                    }.lparams {
                        leftMargin = dip(3)
                        rightMargin = dip(16)
                    }

                    imageView {
                        imageTintList = ColorStateList.valueOf(config.blackAlpha45)
                        setImageResource(R.drawable.ic_info_danmakus)
                    }.lparams(dip(14), dip(14)) {
                        gravity = Gravity.CENTER
                    }
                    textView {
                        textSize = 12f
                        textColor = config.blackAlpha45
                        observeInfo {
                            text = NumberUtil.converString(it!!.stat.danmaku)
                        }
                    }.lparams {
                        leftMargin = dip(3)
                        rightMargin = dip(16)
                    }

                    textView {
                        textSize = 12f
                        textColor = config.blackAlpha45
                        observeInfo {
                            text = NumberUtil.converCTime(it!!.pubdate)
                        }
                    }
                }

                // 视频简介
                mySpannableTextView {
                    observeInfo {
                        setLimitText(it!!.desc)
                    }
                    setOnAvTextClickListener { view, avId ->
                        startFragment(VideoInfoFragment.newInstance(avId))
                    }
                }.lparams {
                    horizontalMargin = dip(10)
                }

                // 分P列表
                linearLayout {
                    frameLayout {
                        recyclerView {
                            val lm = LinearLayoutManager(context)
                            lm.orientation = LinearLayoutManager.HORIZONTAL
                            layoutManager = lm
                            val adapte = createPagesAdapter()
                            val updateView = {
                                this@linearLayout.visibility = if (viewModel.pages.size < 2) View.GONE else View.VISIBLE
                                adapte.notifyDataSetChanged()
                            }
                            viewModel.pages.updateView = updateView
                            updateView.invoke()
                        }.lparams {
                            width = matchParent
                            height = matchParent
                        }
                        view {
                            backgroundResource = R.drawable.shape_gradient
                        }.lparams {
                            gravity = Gravity.RIGHT
                            width = dip(10)
                            height = matchParent
                        }
                    }.lparams {
                        width = matchParent
                        height = matchParent
                        weight = 1f
                    }

                    imageView {
                        setImageResource(R.drawable.ic_navigate_next_black_24dp)
                        selectableItemBackgroundBorderless()
                        setOnClickListener {
                            val fragment = PagesFragment.newInstance(viewModel.id
                                    , viewModel.pages
                                    , viewModel.pageIndex.value!!)
                            MainActivity.of(context)
                                    .showBottomSheet(fragment)
                        }
                    }.lparams(dip(24), dip(24)) {
                        gravity = Gravity.CENTER
                    }
                }.lparams {
                    width = matchParent
                    height = dip(48)
                    margin = dip(10)
                }


                // up主信息
                linearLayout {
                    selectableItemBackground()
                    lparams {
                        margin = dip(10)
                        width = matchParent
                    }
                    setOnClickListener {
                        startFragment(UpperInfoFragment.newInstance(viewModel.info.value!!.owner))
                    }

                    rcImageView {
                        isCircle = true
                        observeInfo {
                            network(it!!.owner.face)
                        }
                    }.lparams {
                        height = dip(40)
                        width = dip(40)
                    }


                    verticalLayout {
                        lparams {
                            leftMargin = dip(8)
                        }
                        textView {
                            textColor = config.black80
                            observeInfo {
                                text = it!!.owner.name
                            }
                        }
                        textView {
                            observeInfo {
                                text = NumberUtil.converString(it!!.owner_ext.fans) + "粉丝"
                            }
                        }
                    }
                }

                createList()
            }
        }

    }

    private fun ViewManager.createList() = recyclerView {
        layoutManager = LinearLayoutManager(context)
        setHasFixedSize(true)
        isNestedScrollingEnabled = false
        miao(viewModel.relates) {
            itemView { binding ->
                linearLayout {
                    lparams(matchParent, wrapContent)
                    selectableItemBackground()
                    padding = dip(5)

                    rcImageView {
                        radius = dip(5)
                        binding.bind { item -> if (item.pic != null) network(item.pic) }
                    }.lparams {
                        width = dip(140)
                        height = dip(85)
                        rightMargin = dip(5)
                    }

                    verticalLayout {
                        textView {
                            ellipsize = TextUtils.TruncateAt.END
                            maxLines = 2
                            textColorResource = R.color.colorForeground
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
                                textColorResource = R.color.black_alpha_45
                                binding.bind { item ->
                                    text = if (item.owner == null) {
                                        ""
                                    } else {
                                        item.owner.name
                                    }
                                }
                            }
                        }

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL
                            imageView {
                                imageResource = R.drawable.ic_play_circle_outline_black_24dp
                            }.lparams {
                                width = dip(16)
                                rightMargin = dip(3)
                            }
                            textView {
                                textSize = 12f
                                textColorResource = R.color.black_alpha_45
                                binding.bind { item -> text = NumberUtil.converString(item.stat.view) }
                            }
                            space().lparams(width = dip(10))
                            imageView {
                                imageResource = R.drawable.ic_subtitles_black_24dp
                            }.lparams {
                                width = dip(16)
                                rightMargin = dip(3)
                            }
                            textView {
                                textSize = 12f
                                textColorResource = R.color.black_alpha_45
                                binding.bind { item -> text = NumberUtil.converString(item.stat.danmaku) }
                            }
                        }

                        linearLayout {

                        }
                    }.lparams(width = matchParent, height = matchParent)
                }
            }
            onItemClick { item, position ->
                startFragment(VideoInfoFragment.newInstance(item.aid.toString()))
//                    IntentHandlerUtil.openWithPlayer(activity!!, IntentHandlerUtil.TYPE_VIDEO, item.id)
            }
        }
    }

    /**
     * 分P列表
     */
    private fun RecyclerView.createPagesAdapter() = miao(viewModel.pages) {
        itemView { b ->
            frameLayout {
                backgroundResource = R.drawable.shape_corner
                lparams(wrapContent, matchParent) {
                    rightMargin = dip(5)
                }

                textView {
                    horizontalPadding = dip(10)
                    verticalPadding = dip(5)
                    textColorResource = R.color.text_black
                    textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                    maxWidth = dip(120)
                    minWidth = dip(60)
                    maxLines = 1
                    gravity = Gravity.LEFT
                    ellipsize = TextUtils.TruncateAt.END
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_START
                    b.bindIndexed { item, index ->
                        if (viewModel.pageIndex.value == index) {
                            this@frameLayout.isEnabled = false
                            textColorResource = config.themeColorResource
                        } else {
                            this@frameLayout.isEnabled = true
                            textColorResource = R.color.text_black
                        }
                        text = item.part
                    }
                }.lparams {
                    gravity = Gravity.CENTER
                }
            }
        }
        onItemClick { item, position ->
            viewModel.pageIndex set position
            VideoInfoFragment.instance.palyVideo(item.cid.toString(), item.part)
        }
        viewModel.pageIndex.observe(this@VideoInfoDetailsFragment
                , Observer { notifyDataSetChanged() })
    }
}