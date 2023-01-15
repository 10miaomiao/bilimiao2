package com.a10miaomiao.bilimiao

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import com.a10miaomiao.bilimiao.comm.compat.*
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.drawable.AutoMirrorDrawable
import com.a10miaomiao.bilimiao.widget.layout.CheckableForegroundLinearLayout
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseProviderMultiAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel


class NavigationListAdapter(
    data: MutableList<NavigationItem>
) : BaseMultiItemQuickAdapter<NavigationListAdapter.NavigationItem, NavigationListAdapter.NavigationViewHolder>(data) {

    init {
        addItemType(ITEM_DEFAULT, R.layout.item_navigation);
        addItemType(ITEM_DIVIDER, R.layout.item_divider);
    }

    fun notifyCheckedChanged() {
        notifyItemRangeChanged(0, itemCount, PAYLOAD_CHECKED_CHANGED)
    }

    override fun convert(holder: NavigationViewHolder, item: NavigationItem) {
        when (item.itemType) {
            ITEM_DEFAULT -> with(holder.defaultItemView) {
                titleText.text = item.title
                if (item.subtitle?.isBlank() != false) {
                    subtitleText.text = ""
                    subtitleText.visibility = View.GONE
                    itemLayout.isChecked = false
                } else {
                    subtitleText.text = item.subtitle
                    subtitleText.visibility = View.VISIBLE
                    itemLayout.isChecked = true
                }
            }
            ITEM_DIVIDER -> with(holder.dividerItemView) { }
        }
    }


    class DefaultItemView(holder: NavigationViewHolder, viewAttributes: ViewAttributes) {
        val itemLayout = holder.getView<CheckableForegroundLinearLayout>(R.id.item_layout)
        val iconImage = holder.getView<ImageView>(R.id.icon_image)
        val textLayout = holder.getView<LinearLayout>(R.id.text_layout)
        val titleText = holder.getView<AppCompatTextView>(R.id.title_text)
        val subtitleText = holder.getView<AppCompatTextView>(R.id.subtitle_text)

        init {
            itemLayout.updatePaddingRelative(
                viewAttributes.itemHorizontalPadding,
                viewAttributes.itemVerticalPadding,
                viewAttributes.itemHorizontalPadding,
                viewAttributes.itemVerticalPadding
            )
            itemLayout.background =
                viewAttributes.itemBackground?.constantState?.newDrawable()
            itemLayout.foregroundCompat =
                viewAttributes.itemForeground?.constantState?.newDrawable()

            iconImage.updateLayoutParams {
                width = viewAttributes.itemIconSize
                height = viewAttributes.itemIconSize
            }
            iconImage.imageTintList = viewAttributes.itemIconTint
            textLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginStart = viewAttributes.itemIconPadding
            }
            if (viewAttributes.itemTextAppearance != ResourcesCompat.ID_NULL) {
                titleText.setTextAppearanceCompat(viewAttributes.itemTextAppearance)
            }
            titleText.setTextColor(viewAttributes.itemTextColor)
            if (viewAttributes.itemSubtitleTextAppearance != ResourcesCompat.ID_NULL) {
                subtitleText.setTextAppearanceCompat(
                    viewAttributes.itemSubtitleTextAppearance
                )
            }
            subtitleText.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, viewAttributes.itemSubtitleTextSize
            )
            subtitleText.setTextColor(viewAttributes.itemSubtitleTextColor)
        }
    }

    class DividerItemView(holder: NavigationViewHolder, viewAttributes: ViewAttributes) {
        val itemLayout = holder.getView<FrameLayout>(R.id.item_layout)
        init {
            itemLayout.updatePaddingRelative(
                viewAttributes.dividerInsetStart,
                viewAttributes.dividerVerticalPadding,
                viewAttributes.dividerInsetEnd,
                viewAttributes.dividerVerticalPadding
            )
        }
    }

    class NavigationViewHolder(val view: View) : BaseViewHolder(view) {
        val defaultItemView by lazy { DefaultItemView(this, viewAttributes) }
        val dividerItemView by lazy { DividerItemView(this, viewAttributes) }

        val context get() = view.context

        @SuppressLint("PrivateResource", "RestrictedApi")
        private val viewAttributes = context.obtainStyledAttributesCompat(
            null, R.styleable.NavigationView, R.attr.navigationViewStyle,
            R.style.Widget_Bilimaio_Material3_NavigationView
        ).use { a ->
            val itemShapeAppearance = a.getResourceId(R.styleable.NavigationView_itemShapeAppearance, 0)
            val itemShapeAppearanceOverlay = a.getResourceId(
                R.styleable.NavigationView_itemShapeAppearanceOverlay, 0
            )
            val itemShapeFillColor = a.getColorStateList(R.styleable.NavigationView_itemShapeFillColor)
            val itemShapeInsetStart =
                a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetStart, 0)
            val itemShapeInsetEnd =
                a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetEnd, 0)
            val itemShapeInsetTop =
                a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetTop, 0)
            val itemShapeInsetBottom =
                a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetBottom, 0)
            val itemBackground = createItemShapeDrawable(
                itemShapeAppearance, itemShapeAppearanceOverlay, itemShapeFillColor,
                itemShapeInsetStart, itemShapeInsetEnd, itemShapeInsetTop, itemShapeInsetBottom, context
            )
            val controlHighlightColor = context.getColorStateListByAttr(R.attr.colorControlHighlight)
            val itemForegroundMaskFillColor = ColorStateList.valueOf(Color.WHITE)
            val itemForegroundMask = createItemShapeDrawable(
                itemShapeAppearance, itemShapeAppearanceOverlay, itemForegroundMaskFillColor,
                itemShapeInsetStart, itemShapeInsetEnd, itemShapeInsetTop, itemShapeInsetBottom, context
            )
            val itemForeground = RippleDrawable(controlHighlightColor, null, itemForegroundMask)
            context.obtainStyledAttributesCompat(
                null, R.styleable.NavigationViewExtra, R.attr.navigationViewStyle, 0
            ).use { a2 ->
                ViewAttributes(
                    a.getDimensionPixelSize(R.styleable.NavigationView_itemHorizontalPadding, 0),
                    a.getDimensionPixelSize(R.styleable.NavigationView_itemVerticalPadding, 0),
                    itemBackground,
                    itemForeground,
                    a.getDimensionPixelSize(R.styleable.NavigationView_itemIconSize, 0),
                    a.getColorStateList(R.styleable.NavigationView_itemIconTint),
                    a.getDimensionPixelSize(R.styleable.NavigationView_itemIconPadding, 0),
                    a.getResourceId(
                        R.styleable.NavigationView_itemTextAppearance, ResourcesCompat.ID_NULL
                    ),
                    a.getColorStateList(R.styleable.NavigationView_itemTextColor),
                    a2.getResourceId(
                        R.styleable.NavigationViewExtra_itemSubtitleTextAppearance,
                        ResourcesCompat.ID_NULL
                    ),
                    a2.getColorStateList(R.styleable.NavigationViewExtra_itemSubtitleTextColor),
                    a2.getDimension(R.styleable.NavigationViewExtra_itemSubtitleTextSize, 0f),
                    a.getDimensionPixelSize(R.styleable.NavigationView_dividerInsetStart, 0),
                    a.getDimensionPixelSize(R.styleable.NavigationView_dividerInsetEnd, 0),
                    a2.getDimensionPixelSize(R.styleable.NavigationViewExtra_dividerVerticalPadding, 0)
                )
            }
        }

        // @see com.google.android.material.navigation.NavigationView#createDefaultItemBackground
        private fun createItemShapeDrawable(
            @StyleRes shapeAppearance: Int,
            @StyleRes shapeAppearanceOverlay: Int,
            fillColor: ColorStateList?,
            @Px insetStart: Int,
            @Px insetEnd: Int,
            @Px insetTop: Int,
            @Px insetBottom: Int,
            context: Context
        ): Drawable {
            val shapeAppearanceModel =
                ShapeAppearanceModel.builder(context, shapeAppearance, shapeAppearanceOverlay).build()
            val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
                .apply { this.fillColor = fillColor }
            return AutoMirrorDrawable(
                InsetDrawable(materialShapeDrawable, insetStart, insetTop, insetEnd, insetBottom)
            )
        }
    }

    class ViewAttributes(
        @Px val itemHorizontalPadding: Int,
        @Px val itemVerticalPadding: Int,
        val itemBackground: Drawable?,
        val itemForeground: Drawable?,
        @Px val itemIconSize: Int,
        val itemIconTint: ColorStateList?,
        @Px val itemIconPadding: Int,
        @StyleRes val itemTextAppearance: Int,
        val itemTextColor: ColorStateList?,
        @StyleRes val itemSubtitleTextAppearance: Int,
        val itemSubtitleTextColor: ColorStateList?,
        @Px val itemSubtitleTextSize: Float,
        @Px val dividerInsetStart: Int,
        @Px val dividerInsetEnd: Int,
        @Px val dividerVerticalPadding: Int
    )

    companion object {
        private val PAYLOAD_CHECKED_CHANGED = Any()
        const val ITEM_DEFAULT = 0
        const val ITEM_DIVIDER = 1
    }

    data class NavigationItem(
        val title: String,
        val subtitle: String? = null,
        override val itemType: Int = ITEM_DEFAULT,
    ): MultiItemEntity

}

