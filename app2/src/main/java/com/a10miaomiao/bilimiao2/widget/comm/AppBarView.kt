package com.a10miaomiao.bilimiao2.widget.comm

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.view.menu.MenuBuilder
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.appcompat.toolbar
import splitties.views.dsl.core.*
import splitties.views.dsl.material.appBarLayout
import splitties.views.imageDrawable
import splitties.views.padding
import splitties.views.topPadding

class AppBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var orientation = LinearLayout.VERTICAL
        set(value) {
            if (field != value) {
                field = value
                mUi = createUi()
                updateProp()
                setView(mUi.root)
            }
        }

    var prop: PropInfo? = null
        set(value) {
            field = value
            updateProp()
        }

    var mUi = createUi()

    init {
        backgroundColor = Color.WHITE
        updateProp()
        setView(mUi.root)
    }


    fun createUi (): ChildUi {
        return if (orientation == ScaffoldView.HORIZONTAL) {
            HorizontalUi(context)
        } else {
            VerticalUi(context)
        }
    }

    fun setView(view: View) {
        removeAllViews()
        addView(view, lParams {
            width = matchParent
            height = matchParent
        })
    }

    private fun updateProp () {
        prop?.let {
            mUi.setProp(prop)
        }
    }

    interface ChildUi : Ui  {
        fun setProp(prop: PropInfo?)
    }

    class VerticalUi(override val ctx: Context) : ChildUi  {

        val mNavigationIcon = imageView {

        }

        val mNavigationIconLayout = frameLayout {
            padding = dip(10)
            addView(mNavigationIcon, lParams {
                width = dip(24)
                height = dip(24)
            })
        }

        val mTitle = textView {

        }

        val mTitleLayout = frameLayout {
            padding = dip(10)
            topPadding = 0
            addView(mTitle, lParams {
                width = matchParent
                height = wrapContent
            })
        }

        override val root = verticalLayout {
            addView(mNavigationIconLayout, lParams {
                width = matchParent
                height = wrapContent
            })
            addView(mTitleLayout, lParams {
                width = matchParent
                height = wrapContent
            })
        }

        override fun setProp(prop: PropInfo?) {
            mNavigationIconLayout.visibility = View.GONE
            mTitleLayout.visibility = View.GONE
            if (prop != null) {
                if (prop.navigationIcon != null) {
                    mNavigationIconLayout.visibility = View.VISIBLE
                    mNavigationIcon.imageDrawable = prop.navigationIcon
                }
                if (prop.title != null) {
                    mTitleLayout.visibility = View.VISIBLE
                    mTitle.text = prop.title ?: ""
                }

            }
        }
    }

    class HorizontalUi(override val ctx: Context) : ChildUi  {
        val mTitle = textView {

        }

        override val root = horizontalLayout {
            addView(mTitle)
        }

        override fun setProp(prop: PropInfo?) {
            if (prop == null) {
                mTitle.text = ""
            } else {
                mTitle.text = prop.title ?: ""
            }
        }
    }

    data class MenuInfo (
        var title: String? = null,
        var icon: Drawable? = null,
    )

    data class PropInfo (
            var title: String? = null,
            var navigationIcon: Drawable? = null,
            var menus: List<MenuInfo>? = null,
    )

}