package cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons

import androidx.compose.ui.graphics.vector.ImageVector
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Bilicoin
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Bilifavourite
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Bililike
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Bilishare
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Danmukunum
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Delete
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Like
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Likefill
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menufold
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menuunfold
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Playnum
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Reply
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Share
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Upper
import kotlin.collections.List as ____KtList

public object CommonGroup

public val BilimiaoIcons.Common: CommonGroup
  get() = CommonGroup

private var __AllIcons: ____KtList<ImageVector>? = null

public val CommonGroup.AllIcons: ____KtList<ImageVector>
  get() {
    if (__AllIcons != null) {
      return __AllIcons!!
    }
    __AllIcons= listOf(Bilicoin, Bilifavourite, Bililike, Bilishare, Danmukunum, Delete, Like,
        Likefill, Menufold, Menuunfold, Playnum, Reply, Share, Upper)
    return __AllIcons!!
  }
