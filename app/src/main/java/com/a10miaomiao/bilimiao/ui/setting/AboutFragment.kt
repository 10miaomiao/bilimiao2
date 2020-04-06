package com.a10miaomiao.bilimiao.ui.setting

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.service.quicksettings.Tile
import android.view.*
import android.widget.TextView
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.utils.selectableItemBackground
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.nestedScrollView

class AboutFragment : SwipeBackFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(createUI().view)
    }


    private fun openUri(uriString: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(uriString)
        startActivity(intent)
    }

    private fun createUI() = UI {
        verticalLayout {
            backgroundColor = config.windowBackgroundColor
            headerView {
                title("关于")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
            }
            nestedScrollView {
                verticalLayout {
                    verticalLayout {
                        gravity = Gravity.CENTER

                        imageView {
                            imageResource = R.mipmap.ic_launcher
                        }.lparams {
                            height = dip(64)
                            width = dip(64)
                            bottomMargin = dip(2)
                        }
                        textView("bilimiao 2.0") {
                            textColor = config.foregroundColor
                            textSize = 16f
                        }.lparams(wrapContent, wrapContent)
                    }.lparams {
                        height = dip(120)
                        width = matchParent
                    }

                    createLine()
                    val version = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0).versionName
                    createItem("版本", "v$version")
                    createLine()
                    view().lparams(height = dip(10))
                    createLine()
                    createItem("作者", "by 10miaomiao.cn") { view ->
                        openUri("https://10miaomiao.cn/")
                    }
                    createLine()
                    createItem("酷安", "_10喵喵"){ view ->
                        openUri("http://www.coolapk.com/u/602470")
                    }
                    createLine()
                    createItem("b站", "10喵喵"){ view ->
                        openUri("https://space.bilibili.com/6789810")
                    }
                    createLine()
                    createItem("Github", "10miaomiao"){ view ->
                        openUri("https://github.com/10miaomiao")
                    }
                    createLine()

                    view().lparams(height = dip(10))
                    createLine()
                    createItem("项目地址", "github.com/10miaomiao/bilimiao2"){ view ->
                        openUri("https://github.com/10miaomiao/bilimiao2")
                    }
                    createLine()
                    createItem("使用声明", getString(R.string.statement))
                    createLine()
                }

            }
        }
    }

    private fun _LinearLayout.createLine() {
        view {
            backgroundColor = config.lineColor
        }.lparams {
            width = matchParent
            height = 2
        }
    }

    private fun ViewManager.createItem(title: String, subTitle: String, onClickListener: ((View) -> Unit)? = null) {
        frameLayout {
            backgroundColor = config.blockBackgroundColor

            linearLayout {
                horizontalPadding = dip(10)
                verticalPadding = dip(10)
                selectableItemBackground()
                if (onClickListener != null)
                    setOnClickListener(onClickListener)

                textView {
                    text = title
                    textSize = 16f
                    textColor = config.foregroundColor
                }

                textView {
                    textSize = 16f
                    text = subTitle
                    textAlignment = TextView.TEXT_ALIGNMENT_TEXT_END
                }.lparams {
                    leftMargin = dip(5)
                    weight = 1f
                    width = matchParent
                }

            }
        }

    }

}