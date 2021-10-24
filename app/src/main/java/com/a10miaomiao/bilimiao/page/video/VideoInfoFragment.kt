package com.a10miaomiao.bilimiao.page.video

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.template.TemplateViewModel
import com.a10miaomiao.bilimiao.widget.rcImageView
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import splitties.dimensions.dip
import splitties.views.dsl.core.*

class VideoInfoFragment: Fragment(), DIAware {

    override val di: DI by DI.lazy {
        bindSingleton { ui }
        bindSingleton { this@VideoInfoFragment }
    }

    private val viewModel by diViewModel<VideoInfoViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun MiaoUI.pageView(): View {
        return verticalLayout {

        }
    }

    fun MiaoUI.upperView(): View {
        return verticalLayout{

        }
    }

    fun MiaoUI.headerView(): View {
        return verticalLayout {

            views {
                +horizontalLayout {
                    views {
                        +rcImageView {
                            radius = dip(5)
//                            observeInfo { network(it!!.pic) }
//                            setOnClickListener {
//                                val info = viewModel.info.value!!
//                                playVideo(
//                                    info.cid.toString(),
//                                    info.title
//                                )
//                            }
                        }..lParams {
                            width = dip(150)
                            height = dip(100)
                            rightMargin = dip(10)
                        }

                        +verticalLayout {

                            views {
                                // 标题
                                +textView {
                                    textSize = 16f
                                    ellipsize = TextUtils.TruncateAt.END
                                    maxLines = 2
                                    setTextColor(config.foregroundColor)
//                                    observeInfo {
//                                        text = it!!.title
//                                    }
                                }..lParams(weight = 1f)

                                // 播放量
                                +horizontalLayout {

                                    views {

                                        +imageView {
                                            imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                                            setImageResource(R.drawable.ic_info_views)
                                        }..lParams(dip(14), dip(14)) {
                                            gravity = Gravity.CENTER
                                        }
                                        +textView {
                                            textSize = 12f
                                            setTextColor(config.foregroundAlpha45Color)
//                                            observeInfo {
//                                                text = NumberUtil.converString(it!!.stat.view)
//                                            }
                                        }..lParams {
                                            leftMargin = dip(3)
                                            rightMargin = dip(16)
                                        }

                                        +imageView {
                                            imageTintList = ColorStateList.valueOf(config.foregroundAlpha45Color)
                                            setImageResource(R.drawable.ic_info_danmakus)
                                        }..lParams(dip(14), dip(14)) {
                                            gravity = Gravity.CENTER
                                        }
                                        +textView {
                                            textSize = 12f
                                            setTextColor(config.foregroundAlpha45Color)
//                                            observeInfo {
//                                                text = NumberUtil.converString(it!!.stat.danmaku)
//                                            }
                                        }..lParams {
                                            leftMargin = dip(3)
                                            rightMargin = dip(16)
                                        }
                                    }

                                }..lParams {
                                    width = matchParent
                                    height = wrapContent
                                    gravity = Gravity.CENTER_VERTICAL
                                }
                            }
                        }..lParams(matchParent, matchParent)


                    }
                }

                +upperView()
                +pageView()..lParams {
                    width = matchParent
                    height = dip(48)
                    margin = dip(10)
                }
            }

//            include<ExpandableTextView>(R.layout.layout_expandable) {
//                observeInfo {
//                    setContent(it!!.desc)
//                }
//                linkClickListener = ExpandableTextView.OnLinkClickListener { linkType, content, selfContent -> //根据类型去判断
//                    when (linkType) {
//                        LinkType.LINK_TYPE -> {
//                            viewModel.toLink(content)
//                        }
//                        LinkType.MENTION_TYPE -> {
//                            toast("你点击了@用户 内容是：$content")
//                        }
//                        LinkType.SELF -> {
//                            viewModel.toLink(selfContent)
//                        }
//                    }
//                }
//            }.lparams {
//                horizontalMargin = dip(10)
//            }


        }
    }

    val ui = miaoBindingUi {
        verticalLayout {
            views {
                +headerView()..lParams {
                    height = wrapContent
                    width = matchParent
                }
            }
        }
    }

}