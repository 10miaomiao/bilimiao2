package cn.a10miaomiao.bilimiao.compose.assets

import androidx.compose.ui.graphics.vector.ImageVector
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.AllIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import kotlin.collections.List as ____KtList

public object BilimiaoIcons

private var __AllIcons: ____KtList<ImageVector>? = null

public val BilimiaoIcons.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= Common.AllIcons + listOf()
    return __AllIcons!!
  }
