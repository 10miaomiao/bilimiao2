package com.a10miaomiao.bilimiao.comm.entity.auth

data class TmpUserInfo(
    val account_info: AccountInfo,
) {

    data class AccountInfo(
        val hide_tel: String,
    )
}