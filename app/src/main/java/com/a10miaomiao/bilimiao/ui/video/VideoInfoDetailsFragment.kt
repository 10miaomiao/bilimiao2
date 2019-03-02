package com.a10miaomiao.bilimiao.ui.video

import android.app.Activity
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.rcLayout
import com.a10miaomiao.bilimiao.utils.*
import com.a10miaomiao.miaoandriod.adapter.miao
import com.a10miaomiao.miaoandriod.anko.liveUI
import kotlinx.android.synthetic.main.fragment_video_info.view.*
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
        viewModel = ViewModelProviders.of(VideoInfoFragment.instance, newViewModelFactory { VideoInfoViewModel(id) })
                .get(VideoInfoViewModel::class.java)
        return createUI().view
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
                    rcLayout {
                        roundCorner = dip(5)
                        imageView {
                            // scaleType = ImageView.ScaleType.CENTER
                            binding.bind { item -> network(item.pic) }
                        }.lparams(matchParent, matchParent)
                    }.lparams(width = dip(140), height = dip(85)) {
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
                                    text = if(item.owner == null){
                                        ""
                                    }else{
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
            }.onItemClick { item, position ->
                startFragment(VideoInfoFragment.newInstance(item.aid.toString()))
//                    IntentHandlerUtil.openWithPlayer(activity!!, IntentHandlerUtil.TYPE_VIDEO, item.id)
            }
        }
    }

    private fun createUI() = liveUI(viewModel.info) {
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
                    observeNotNull {
                        text = it.title
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
                        observeNotNull {
                            text = NumberUtil.converString(it.stat.view)
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
                        observeNotNull {
                            text = NumberUtil.converString(it.stat.danmaku)
                        }
                    }.lparams {
                        leftMargin = dip(3)
                        rightMargin = dip(16)
                    }

                    textView {
                        textSize = 12f
                        textColor = config.blackAlpha45
                        observeNotNull {
                            text = NumberUtil.converCTime(it.pubdate)
                        }
                    }
                }

                // 视频简介
                textView {
                    observeNotNull {
                        text = it.desc
                    }
                }.lparams {
                    horizontalMargin = dip(10)
                }

                // up主信息
                linearLayout {
                    lparams {
                        margin = dip(10)
                    }
                    rcLayout {
                        mRoundAsCircle = true
                        imageView {
                            ::network.bind { it.owner.face }
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
                            observeNotNull {
                                text = it.owner.name
                            }
                        }
                        textView {
                            observeNotNull {
                                text = NumberUtil.converString(it.owner_ext.fans) + "粉丝"
                            }
                        }
                    }
                }

                createList()
            }
        }

    }
}