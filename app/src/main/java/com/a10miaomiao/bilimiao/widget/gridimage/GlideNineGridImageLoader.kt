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

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.transition.Transition

class GlideNineGridImageLoader : INineGridImageLoader {

    internal class ProportionalZoomTarget(
        private val imageView: ImageView,
        private val onlyOneSize: Int,
        private val proportionalZoomCallback: ProportionalZoomCallback
    ) : BitmapImageViewTarget(imageView) {

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            val originalImageWidth = resource.width // 原图宽度
            val originalImageHeight = resource.height // 原图高度
            val radio = originalImageWidth.toFloat() / originalImageHeight.toFloat()

            /**
             * 等比缩放计算规则：
             *
             * 1. 图片宽度比高度大，则高度为设置的值，宽度根据配置和图片比例求得
             * 2. 图片宽度比高度小，则宽度为设置的值，高度根据配置和图片比例求得
             */
            val calWidth: Int
            val calHeight: Int
            if (originalImageWidth > originalImageHeight) {
                calHeight = onlyOneSize
                calWidth = (calHeight * radio).toInt()
            } else {
                calWidth = onlyOneSize
                calHeight = (calWidth / radio).toInt()
            }

            // 将计算好的值回调，重新进行布局
            proportionalZoomCallback.sizeChange(imageView, calWidth, calHeight)

            super.onResourceReady(resource, transition)
        }

    }

    override fun displayOnlyOneImage(
        imageView: ImageView,
        url: String,
        position: Int,
        onlyOneSize: Int,
        proportionalZoomCallback: ProportionalZoomCallback
    ) {
        Glide.with(imageView)
            .asBitmap()
            .load(url)
//            .placeholder(R.drawable.ic_baseline_hourglass_bottom)
//            .error(R.drawable.ic_baseline_broken_image)
            .into(ProportionalZoomTarget(imageView, onlyOneSize, proportionalZoomCallback))
    }

    override fun displayImage(
        imageView: ImageView,
        url: String,
        urlList: List<String>,
        position: Int,
        isShowMore: Boolean
    ) {
        if (isShowMore) {
            val moreSize = urlList.size - (position + 1)
            val desc = "+${
                if (moreSize > 99) {
                    "99"
                } else {
                    moreSize
                }
            }"
            Glide.with(imageView)
                .load(url)
                .transform(
                    MoreCoverLayer(desc)
                )
//                .placeholder(R.drawable.ic_baseline_hourglass_bottom)
//                .error(R.drawable.ic_baseline_broken_image)
                .into(imageView)
        } else {
            Glide.with(imageView)
                .load(url)
//                .placeholder(R.drawable.ic_baseline_hourglass_bottom)
//                .error(R.drawable.ic_baseline_broken_image)
                .into(imageView)
        }
    }
}