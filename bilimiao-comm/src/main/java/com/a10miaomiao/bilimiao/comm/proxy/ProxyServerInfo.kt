package com.a10miaomiao.bilimiao.comm.proxy

data class ProxyServerInfo (
    val name: String, // 服务器名字
    val host: String, // 服务器host
    val isTrust: Boolean, // 是否信任服务器(是否携带B站token)
)