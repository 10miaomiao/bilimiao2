package com.a10miaomiao.bilimiao.drawable

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import net.mikaelzero.mojito.view.sketch.core.util.DrawableWrapper

@SuppressLint("RestrictedApi")
class AutoMirrorDrawable(drawable: Drawable) : DrawableWrapper(drawable) {
    override fun draw(canvas: Canvas) {
        if (needMirroring()) {
            val centerX = bounds.exactCenterX()
            canvas.scale(-1f, 1f, centerX, 0f)
            super.draw(canvas)
            canvas.scale(-1f, 1f, centerX, 0f)
        } else {
            super.draw(canvas)
        }
    }

    override fun onLayoutDirectionChanged(layoutDirection: Int): Boolean {
        super.onLayoutDirectionChanged(layoutDirection)

        return true
    }

    override fun isAutoMirrored(): Boolean = true

    private fun needMirroring(): Boolean = DrawableCompat.getLayoutDirection(this) == View.LAYOUT_DIRECTION_RTL

    override fun getPadding(padding: Rect): Boolean {
        val hasPadding = super.getPadding(padding)
        if (needMirroring()) {
            val paddingStart = padding.left
            val paddingEnd = padding.right
            padding.left = paddingEnd
            padding.right = paddingStart
        }
        return hasPadding
    }

    override fun getConstantState(): ConstantState? =
        wrappedDrawable?.constantState?.let { DelegateConstantState(it) }

    private class DelegateConstantState(
        private val constantState: ConstantState
    ) : ConstantState() {
        override fun newDrawable(): Drawable = AutoMirrorDrawable(constantState.newDrawable())

        override fun newDrawable(res: Resources?): Drawable =
            AutoMirrorDrawable(constantState.newDrawable(res))

        override fun newDrawable(res: Resources?, theme: Resources.Theme?): Drawable =
            AutoMirrorDrawable(constantState.newDrawable(res, theme))

        override fun getChangingConfigurations(): Int = constantState.changingConfigurations

        override fun canApplyTheme(): Boolean = constantState.canApplyTheme()
    }
}
