package com.a10miaomiao.bilimiao.comm.entity.user

data class UserEmotePanelInfo (
    val setting: UserEmotePanelSettingInfo,
    val packages: List<UserEmotePackageInfo>,
)