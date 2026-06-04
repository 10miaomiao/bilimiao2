package com.a10miaomiao.bilimiao.comm.entity.user

import kotlinx.serialization.Serializable

@Serializable
data class UserEmotePanelInfo (
//    val setting: UserEmotePanelSettingInfo,
    val packages: List<UserEmotePackageInfo>,
)