package com.a10miaomiao.bilimiao.comm.entity.auth

data class LoginInfo(
    val cookie_info: CookieInfo?,
    val sso: List<String>?,
    val status: Int,
    val message: String?,
    val url: String?,
    val token_info: TokenInfo
){
    data class CookieInfo(
        val cookies: List<Cookie>,
        val domains: List<String>
    )

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

    data class TokenInfo(
        val access_token: String,
        val expires_in: Int,
        val mid: Long,
        val refresh_token: String
    )
}