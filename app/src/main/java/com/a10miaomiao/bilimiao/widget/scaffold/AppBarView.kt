package com.a10miaomiao.bilimiao.widget.scaffold

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.widget.scaffold.ui.AppBarHorizontalUi
import com.a10miaomiao.bilimiao.widget.scaffold.ui.AppBarUi
import com.a10miaomiao.bilimiao.widget.scaffold.ui.AppBarVerticalUi
import splitties.views.dsl.core.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class AppBarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var canBack = false
    var showPointer = false
    var pointerOrientation = true
    var onBackClick: View.OnClickListener? = null
    var onBackLongClick: View.OnLongClickListener? = null
    var onMenuItemClick: ((MenuItemView) -> Unit)? = null
    var onPointerClick: View.OnClickListener? = null
    var onPointerLongClick: View.OnLongClickListener? = null
    var onExchangeClick: View.OnClickListener? = null
    var onExchangeLongClick: View.OnLongClickListener? = null

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
    private val pointerClick = OnClickListener {view ->
        onPointerClick?.onClick(view)
    }
    private val pointerLongClick = OnLongClickListener { view ->
        onPointerLongClick?.onLongClick(view) ?: false
    }
    private val exchangeClick = OnClickListener {view ->
        onExchangeClick?.onClick(view)
    }
    private val exchangeLongClick = OnLongClickListener {view ->
        onExchangeLongClick?.onLongClick(view) ?: false
    }

    private var mUi = createUi()

    init {
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
                pointerClick = pointerClick,
                pointerLongClick = pointerLongClick,
                exchangeClick = exchangeClick,
                exchangeLongClick = exchangeLongClick,
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
        if (childCount > 0) {
            removeAllViews()
        }
        addView(view, 0, lParams {
            width = matchParent
            height = matchParent
        })
    }

    fun updateTheme() {
        mUi.updateTheme()
    }

    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        if (orientation == ScaffoldView.VERTICAL) {
            setPadding(
                left, 0, right, bottom
            )
        } else {
            setPadding(
                left, top, 0, bottom
            )
        }
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun newProp (): PropInfo {
        val prop = PropInfo()
        if (canBack) {
//            prop.onNavigationClick = onBackClick
            prop.navigationIcon = resources.getDrawable(R.drawable.ic_back_24dp)
        }
        if (showPointer) {
            prop.navigationPointerIcon = resources.getDrawable(R.drawable.ic_pointer_24dp)
            prop.pointerIconOrientation = pointerOrientation
        }
        prop.navigationExchangeIcon = resources.getDrawable(R.drawable.ic_exchange_24dp)
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
        var navigationPointerIcon: Drawable? = null,
        var pointerIconOrientation: Boolean = true,
        var navigationExchangeIcon: Drawable? = null,
        var menus: List<MenuItemPropInfo>? = null,
    )

}