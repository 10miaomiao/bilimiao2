package com.a10miaomiao.bilimiao.comm.entity.user

import kotlinx.serialization.Serializable

@Serializable
data class UserEmotePackagesInfo (
    val packages: List<UserEmotePackageInfo>,
)