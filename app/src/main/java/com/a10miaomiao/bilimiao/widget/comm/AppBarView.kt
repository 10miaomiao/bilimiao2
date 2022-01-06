package com.a10miaomiao.bilimiao.widget.comm

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Menu
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.forEach
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.widget.comm.ui.AppBarHorizontalUi
import com.a10miaomiao.bilimiao.widget.comm.ui.AppBarUi
import com.a10miaomiao.bilimiao.widget.comm.ui.AppBarVerticalUi
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.imageDrawable
import splitties.views.padding
import splitties.views.topPadding

class AppBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var canBack = false
    var onBackClick: View.OnClickListener? = null
    var onMenuItemClick: ((MenuItemView) -> Unit)? = null

    var orientation = ScaffoldView.VERTICAL
        set(value) {
            if (field != value) {
                field = value
                mUi = createUi()
                updateProp()
                setView(mUi.root)
            }
        }

    private var prop: PropInfo? = null
        set(value) {
            field = value
            updateProp()
        }

    private val menuItemClick = OnClickListener { view ->
        (view as? MenuItemView)?.let {
            onMenuItemClick?.invoke(it)
        }
    }

    private var mUi = createUi()

    init {
        backgroundColor = Color.WHITE
        updateProp()
        setView(mUi.root)
    }


    fun createUi (): AppBarUi {
        return if (orientation == ScaffoldView.HORIZONTAL) {
            AppBarHorizontalUi(context, menuItemClick)
        } else {
            AppBarVerticalUi(context, menuItemClick)
        }
    }

    fun setView(view: View) {
        removeAllViews()
        addView(view, lParams {
            width = matchParent
            height = matchParent
        })
    }

    fun cleanProp() {
        this.prop = newProp()
    }

    fun setProp(block: PropInfo.() -> Unit) {
        val prop = newProp()
        prop.block()
        this.prop = prop
    }

    private fun newProp (): PropInfo {
        val prop = PropInfo()
        if (canBack) {
            prop.onNavigationClick = onBackClick
            prop.navigationIcon = resources.getDrawable(R.drawable.ic_back_24dp)
        }
        return prop
    }

    private fun updateProp () {
        prop?.let {
            mUi.setProp(prop)
        }
    }


    class PropInfo (
        var title: String? = null,
        var navigationIcon: Drawable? = null,
        var menus: List<MenuItemView.MenuItemPropInfo>? = null,
        var onNavigationClick: View.OnClickListener? = null,
    )

}