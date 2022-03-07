package com.a10miaomiao.bilimiao.widget

import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView


inline fun ExpandableTextView._setContent(value: String) = miaoEffect(value) {
    setContent(it)
}