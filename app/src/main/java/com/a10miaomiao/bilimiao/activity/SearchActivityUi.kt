package com.a10miaomiao.bilimiao.activity

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.config.config
import com.google.android.material.card.MaterialCardView
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.editText
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.radioButton
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.core.wrapInHorizontalScrollView
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.horizontalPadding


class SearchActivityUi(
    val activity: SearchActivity,
    val themeDelegate: ThemeDelegate,
    val keyword: String,
    val mode: Int,
    val selfSearchName: String?,
) : Ui {

    private val iconSize = activity.dip(30)

    override val ctx = activity

    private val colorStateList = ColorStateList(
        arrayOf(
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_checked)
        ),
        intArrayOf(
            activity.config.foregroundAlpha45Color,
            themeDelegate.themeColor.toInt()
        )
    )

    val allRadioButton = radioButton {
        text = "搜索全站"
        isChecked = mode == 0 || selfSearchName.isNullOrBlank()
        buttonTintList = colorStateList
    }

    val selfRadioButton = radioButton {
        if (selfSearchName.isNullOrBlank()) {
            visibility = View.GONE
        } else {
            text = selfSearchName
            isChecked = mode == 1
            visibility = View.VISIBLE
        }
        buttonTintList = colorStateList
    }

    val searchEditText = editText {
        textSize = 18f
        hint = "请输入ID或关键字"
        horizontalPadding = iconSize + dip(15)
        setBackgroundResource(0)
        imeOptions = EditorInfo.IME_ACTION_SEARCH
        isSingleLine = true
        inputType = EditorInfo.TYPE_CLASS_TEXT
//        setOnEditorActionListener(handleEditorAction)

        // 默认值
        setText(keyword)
        setSelection(keyword.length)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    val searchIcon = imageView {
        setImageResource(R.drawable.ic_search_24dp)
        setBackgroundResource(config.selectableItemBackgroundBorderless)
    }

    val searchCloseIcon = imageView {
        setImageResource(R.drawable.ic_close_grey_24dp)
        setBackgroundResource(config.selectableItemBackgroundBorderless)
    }

    @OptIn(InternalSplittiesApi::class)
    val searchBoxView = view<MaterialCardView>() {
        transitionName = "shareElement"
        
        setCardBackgroundColor(config.blockBackgroundColor)
        strokeWidth = 0
        val iconSize = dip(30)
        radius = dip(10f)

        addView(verticalLayout {
            addView(
                horizontalLayout {
                    addView(allRadioButton, lParams { rightMargin = config.dividerSize })
                    addView(selfRadioButton)
                }.wrapInHorizontalScrollView(
                    height = dip(40),
                    initView = {
                        overScrollMode = View.OVER_SCROLL_NEVER
                    }
                ), lParams(matchParent, wrapContent)
            )

            addView(frameLayout {
                addView(searchEditText, lParams(matchParent, dip(60)))
                addView(searchIcon, lParams(iconSize, iconSize) {
                    leftMargin = config.pagePadding
                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                })
                addView(searchCloseIcon, lParams(iconSize, iconSize) {
                    rightMargin = config.pagePadding
                    gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
                })
            }, lParams(matchParent, dip(60)))
        }, lParams(matchParent, matchParent))
    }


    val suggestRecycler = recyclerView {
        backgroundColor = config.windowBackgroundColor
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
    }


    override val root = verticalLayout {
//        transitionName = "shareElement"
        backgroundColor = config.windowBackgroundColor
        fitsSystemWindows = true
        clipToPadding = true

        layoutParams = ViewGroup.LayoutParams(matchParent, matchParent)
        addView(suggestRecycler, lParams(matchParent, matchParent) {
            weight = 1f
        })
        addView(searchBoxView, lParams(matchParent, wrapContent))
    }

}