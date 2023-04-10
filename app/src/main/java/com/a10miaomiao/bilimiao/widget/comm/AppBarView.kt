package com.a10miaomiao.bilimiao.widget.comm

import android.annotation.SuppressLint
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
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.widget.comm.ui.AppBarHorizontalUi
import com.a10miaomiao.bilimiao.widget.comm.ui.AppBarUi
import com.a10miaomiao.bilimiao.widget.comm.ui.AppBarVerticalUi
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import splitties.views.imageDrawable
import splitties.views.padding
import splitties.views.topPadding
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class AppBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var canBack = false
    var onBackClick: View.OnClickListener? = null
    var onBackLongClick: View.OnLongClickListener? = null
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

    private val backClick = OnClickListener { view ->
        onBackClick?.onClick(view)

    }
    private val backLongClick = OnLongClickListener { view ->
        onBackLongClick?.onLongClick(view) ?: false
    }

    private val menuItemClick = OnClickListener { view ->
        (view as? MenuItemView)?.let {
            if (it.prop.key == MenuKeys.back) {
                onBackClick?.onClick(view)
            } else {
                onMenuItemClick?.invoke(it)
            }
        }
    }
    private val menuItemLongClick = OnLongClickListener { view ->
        if (view is MenuItemView
            && view.prop.key == MenuKeys.back) {
            onBackLongClick?.onLongClick(view) ?: false
        } else {
            false
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
            AppBarHorizontalUi(
                context,
                menuItemClick = menuItemClick,
                menuItemLongClick = menuItemLongClick,
                backClick = backClick,
                backLongClick = backLongClick,
            )
        } else {
            AppBarVerticalUi(
                context,
                menuItemClick = menuItemClick,
                menuItemLongClick = menuItemLongClick,
//                backClick = backClick,
//                backLongClick = backLongClick,
            )
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

    @OptIn(ExperimentalContracts::class)
    @SuppressLint("RestrictedApi")
    fun setProp(block: PropInfo.() -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        val prop = newProp()
        prop.block()
        this.prop = prop
    }

    private fun newProp (): PropInfo {
        val prop = PropInfo()
        if (canBack) {
//            prop.onNavigationClick = onBackClick
            prop.navigationIcon = resources.getDrawable(R.drawable.ic_back_24dp)
        }
        return prop
    }

    private fun updateProp () {
        prop?.let {
            mUi.setProp(prop)
        }
    }

    fun showMenu() {
        (mUi as? AppBarVerticalUi)?.showMenu()
    }

    fun hideMenu() {
        (mUi as? AppBarVerticalUi)?.hideMenu()
    }


    class PropInfo (
        var title: String? = null,
        var navigationIcon: Drawable? = null,
        var menus: List<MenuItemPropInfo>? = null,
    )

}