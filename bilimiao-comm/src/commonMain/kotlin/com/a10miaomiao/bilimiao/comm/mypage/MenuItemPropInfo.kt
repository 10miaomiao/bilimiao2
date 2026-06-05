package com.a10miaomiao.bilimiao.comm.mypage

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuItemPropInfo (
    var key: Int? = null,
    var action: String? = null,
    var title: String? = null,
    var subTitle: String? = null,
    var iconVector: ImageVector? = null,
    var visibility: Int = 0,
    var childMenu: MyPageMenu? = null,
    var contentDescription: String? = null,
) {

}
