/*
    MIT License
    Copyright (c) 2021 Plain
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
/*
    来源：https://github.com/plain-dev/NineGridImageView
 */
package com.a10miaomiao.bilimiao.widget.gridimage

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import java.util.*
import kotlin.math.min

class NineGridImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr), ProportionalZoomCallback {

    var externalPosition: Int = -1
    var onImageItemClickListener: OnImageItemClickListener? = null
    var imageLoader: INineGridImageLoader? = null

    var spacing: Int = 0
    var onlyOneSize: Int = 0

    private var imageWidth: Int = 0
    private var columns: Int = 0
    private var rows: Int = 0
    private var totalWidth = 0

    private var isFirstDraw: Boolean = true

    private val imageUrlList: MutableList<String> = mutableListOf()

    init {
//        context.obtainStyledAttributes(attrs, R.styleable.NineGridImageView).apply {
//            spacing = getDimension(
//                R.styleable.NineGridImageView_ng_spacing,
//                10f.px
//            )
//            onlyOneSize = getDimensionPixelSize(
//                R.styleable.NineGridImageView_ng_only_one_size,
//                0
//            )
//        }.recycle()
        changeVisibility()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        totalWidth = right - left
        imageWidth = getSingleWidth()
        if (isFirstDraw) {
            notifyDataSetChanged()
            isFirstDraw = false
        }
    }

    private fun notifyDataSetChanged() {
        post(object : TimerTask() {
            override fun run() {
                refresh()
            }
        })
    }

    private fun refresh() {
        removeAllViews()

        val size: Int = getListSize(imageUrlList)
        if (size > 0) {
            visibility = VISIBLE
        } else {
            visibility = GONE
            return
        }

        if (size == 1) { // 处理单张图片的情况
            val url: String = imageUrlList[0]
            val imageView = createImageView(0, url)
            val singleImageSize: Int = getSingleImageSize()
            // 加载图片前先设置一下图片的大小 (正方形)
            setSingleImageSize(imageView, singleImageSize, singleImageSize)
            addView(imageView)
            imageLoader?.displayOnlyOneImage(
                imageView,
                url,
                0,
                singleImageSize,
                this
            )
            return
        }

        // 1. 根据图片个数确定行数和列数
        generateChildrenLayout(size)
        // 2. 结合行数和列表等参数，设置父容器大小
        setParentContainerSize()
        // 3. 创建图片视图，并显示到父容器中
        val validSize = min(size, 9)
        for (position in 0 until validSize) {
            val url = imageUrlList[position]
            val imageView: ImageView = createImageView(position, url)
            layoutImageView(imageView, url, position)
        }
    }

    private fun generateChildrenLayout(size: Int) {
        if (size <= 3) {
            rows = 1
            columns = size
        } else if (size <= 6) {
            rows = 2
            columns = 3
            if (size == 4) {
                columns = 2
            }
        } else {
            columns = 3
            rows = 3
        }
    }

    private fun setParentContainerSize() {
        val params = layoutParams
        params.height = (imageWidth * rows + spacing * (rows - 1))
        layoutParams = params
    }

    private fun layoutImageView(imageView: ImageView, url: String, position: Int) {
        val singleWidth = getSingleWidth()

        // 拿到该图片所在行和列坐标
        val internalPosition: IntArray = findRowColumnByPosition(position)
        // 求得图片所处位置
        val left = ((singleWidth + spacing) * internalPosition[1]).toInt()
        val top = ((singleWidth + spacing) * internalPosition[0]).toInt()
        val right = left + singleWidth
        val bottom = top + singleWidth

        imageView.layout(left, top, right, bottom)
        addView(imageView)

        imageLoader?.displayImage(
            imageView,
            url,
            imageUrlList,
            position,
            imageUrlList.size > 9 && position == 8
        )
    }

    private fun findRowColumnByPosition(position: Int): IntArray {
        val location = IntArray(2)
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                if (i * columns + j == position) {
                    location[0] = i // Row
                    location[1] = j // Column
                    break
                }
            }
        }
        return location
    }

    override fun sizeChange(imageView: ImageView, width: Int, height: Int) {
        setSingleImageSize(imageView, width, height)
    }

    private fun setSingleImageSize(
        imageView: ImageView,
        width: Int,
        height: Int
    ) {
        imageView.layout(0, 0, width, height)
        val params = layoutParams
        params.height = height
        layoutParams = params
    }

    private fun getSingleImageSize() = if (onlyOneSize <= 0) {
        imageWidth
    } else {
        onlyOneSize
    }

    private fun createImageView(position: Int, url: String) = ImageView(context).apply {
        id = View.generateViewId()
        scaleType = ImageView.ScaleType.CENTER_CROP
        setOnClickListener {
            onImageItemClickListener?.onClick(
                this@NineGridImageView,
                this,
                url,
                imageUrlList,
                externalPosition,
                position
            )
        }
        setOnLongClickListener { true }
    }

    private fun getSingleWidth(): Int {
        return ((totalWidth - spacing * (3 - 1)) / 3)
    }

    private fun changeVisibility() {
        visibility = if (imageUrlList.isEmpty()) {
            GONE
        } else {
            VISIBLE
        }
    }

    private fun getListSize(list: List<String>?): Int {
        return list?.size ?: 0
    }

    fun getImageViews(): List<ImageView> {
        val imageViews = mutableListOf<ImageView>()
        for (i in 0 until childCount) {
            val imageView = getChildAt(i)
            if (imageView is ImageView) {
                imageViews.add(imageView)
            }
        }
        return imageViews
    }

    fun getImageViewAt(position: Int) = getChildAt(position) as? ImageView

    fun setUrlList(urlList: List<String>?) {
        if (urlList.isNullOrEmpty()) {
            visibility = GONE
            return
        }

        visibility = VISIBLE

        imageUrlList.clear()
        imageUrlList.addAll(urlList)

        if (!isFirstDraw) {
            notifyDataSetChanged()
        }
    }

}