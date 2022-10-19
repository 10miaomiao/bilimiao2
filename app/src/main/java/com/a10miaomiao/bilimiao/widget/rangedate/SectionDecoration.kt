package com.a10miaomiao.bilimiao.widget.rangedate


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author zhangqy
 * @Description 日期月份title渲染
 * @date 2018/7/6
 */
class SectionDecoration(var context: Context, private var callback: DecorationCallback?) : RecyclerView.ItemDecoration() {

    private var textPaint: TextPaint
    private var paint: Paint = Paint()

    private var topGap: Int = 0 //高度
    private var fontMetrics: Paint.FontMetrics
    private var topPadding: Float = 0f

    interface DecorationCallback {

        fun getGroupId(position: Int): String
    }

    init {
        //设置悬浮栏的画笔---paint
        paint.color = Color.WHITE

        //设置悬浮栏中文本的画笔
        textPaint = TextPaint()
        textPaint.isAntiAlias = true
        textPaint.textSize = RangeDateUtils.sp2px(context, 16f).toFloat()
        textPaint.color = Color.parseColor("#333333")
        textPaint.textAlign = Paint.Align.CENTER
        fontMetrics = textPaint.fontMetrics
        topGap = RangeDateUtils.dp2px(context, 55).toInt()

        topPadding = -((fontMetrics.bottom - fontMetrics.top) / 2 + fontMetrics.top)
    }

    //    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
    //        super.getItemOffsets(outRect, view, parent, state)
    //
    //        val pos = parent.getChildAdapterPosition(view)
    //        if (pos == RecyclerView.NO_POSITION) {
    //            return
    //        }
    //
    //        outRect.top = if (isFirstInGroup(pos)) topGap else 0
    //    }


    //    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
    //        val childCount = parent.childCount
    //
    //        for (i in 0 until childCount) {
    //            val view = parent.getChildAt(i)
    //            val position = parent.getChildAdapterPosition(view)
    //
    //            if (position == RecyclerView.NO_POSITION) {
    //                continue
    //            }
    //
    //            if (isFirstInGroup(position)) {
    //                //绘制悬浮栏
    //                //                val rect = RectF(
    //                //                        parent.paddingLeft.toFloat(),
    //                //                        (view.top - topGap).toFloat(),
    //                //                        (parent.width - parent.paddingRight).toFloat(),
    //                //                        view.top.toFloat()
    //                //                )
    //                val rect = RectF(
    //                        parent.paddingLeft.toFloat(),
    //                        view.top.toFloat(),
    //                        (parent.width - parent.paddingRight).toFloat(),
    //                        (view.top + topGap).toFloat()
    //                )
    //                c.drawRect(rect, paint)
    //
    //                //绘制文本
    //                c.drawText(callback?.getGroupFirstLine(position), rect.centerX(), rect.centerY() + topPadding, textPaint)
    //            }
    //        }
    //    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val position = (parent.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
        if (position == RecyclerView.NO_POSITION) {
            return
        }

        val child = parent.findViewHolderForAdapterPosition(position)?.itemView

        var flag = false
        if (isLastInGroup(position) && null != child) {
            if (child.height + child.top < topGap) {
                c.save()
                flag = true
                c.translate(0f, (child.height + child.top - topGap).toFloat())
            }
        }

        val rect = RectF(
            parent.paddingLeft.toFloat(),
            parent.paddingTop.toFloat(),
            (parent.right - parent.paddingRight).toFloat(),
            (parent.paddingTop + topGap).toFloat()
        )
        c.drawRect(rect, paint)

        c.drawText(callback?.getGroupId(position) ?: "",
            rect.centerX(),
            rect.centerY() + topPadding,
            textPaint)

        if (flag) {
            c.restore()
        }
    }

    /**
     * 判断是不是组中的第一个位置
     */
    private fun isFirstInGroup(pos: Int): Boolean {
        if (pos <= 0) {
            return true
        }

        return callback?.getGroupId(pos - 1) != callback?.getGroupId(pos)
    }

    /**
     * 是否是组中最后一个
     */
    private fun isLastInGroup(pos: Int): Boolean {
        //        val cur = callback?.getGroupId(pos)
        //        val next = callback?.getGroupId(pos + 7)
        //
        //        Log.e("isLastInGroup", "$pos -> $cur | $next")
        //
        //        return cur != next
        return callback?.getGroupId(pos) != callback?.getGroupId(pos + 7)
    }

}