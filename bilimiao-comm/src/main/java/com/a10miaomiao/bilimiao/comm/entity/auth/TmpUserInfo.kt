package com.a10miaomiao.bilimiao.comm.entity.auth

import kotlinx.serialization.Serializable

/**
 * {"account_info":{"hide_tel":"131*****810","hide_mail":"103*****qq.com","bind_mail":true,"bind_tel":true,"tel_verify":true,"mail_verify":true,"unneeded_check":false},"member_info":{"nickname":"10喵喵","face":"https://i0.hdslb.com/bfs/face/d4cce4661c255c3277b42894319dd6631bd06d5c.jpg"},"sns_info":{"bind_google":false,"bind_fb":false,"bind_apple":false}}
 */
@Serializable
data class TmpUserInfo(
    val account_info: AccountInfo,
) {
    @Serializable
    data class AccountInfo(
        val hide_tel: String,
        val hide_mail: String,
        val bind_tel: Boolean,
        val bind_mail: Boolean,
        val tel_verify: Boolean,
        val mail_verify: Boolean,
    )
}