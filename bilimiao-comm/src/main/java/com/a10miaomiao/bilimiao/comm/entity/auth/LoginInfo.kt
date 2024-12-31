package com.a10miaomiao.bilimiao.comm.entity.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginInfo(
    val token_info: TokenInfo,
    val sso: List<String>?,
    val cookie_info: CookieInfo?,
){

    @Serializable
    data class PasswordLoginInfo(
        val cookie_info: CookieInfo? = null,
        val sso: List<String>? = null,
        val status: Int,
        val message: String,
        val url: String,
        val token_info: TokenInfo? = null,
    ){
        fun toLoginInfo() = LoginInfo(
            token_info = token_info!!,
            cookie_info = cookie_info,
            sso = sso,
        )
    }

    @Serializable
    data class QrLoginInfo(
        val is_new: Boolean,
        val mid: Long,
        val access_token: String,
        val expires_in: Int,
        val refresh_token: String,
        val token_info: TokenInfo,
        val sso: List<String>?,
        val cookie_info: CookieInfo?,
    ){
        fun toLoginInfo() = LoginInfo(
            token_info = token_info,
            cookie_info = cookie_info,
            sso = sso,
        )
    }

    @Serializable
    data class CookieInfo(
        val cookies: List<Cookie>,
        val domains: List<String>
    )

    @Serializable
    data class Cookie(
        val expires: Int,
        val http_only: Int,
        val name: String,
        val value: String
    ){
        fun getValue(domain: String): String{
            return "$name=$value;Expires=$expires;Domain=$domain;${if (http_only == 1) "HTTPOnly;" else ""}"
        }
    }

    @Serializable
    data class TokenInfo(
        val access_token: String,
        val expires_in: Int,
        val mid: Long,
        val refresh_token: String
    )
}