package com.a10miaomiao.bilimiao.comm.entity

data class ResultListInfo<T> (
    var code: Int,
    var msg: String,
    var data: List<T>
) {
    // 响应结果是否为成功
    val isSuccess get() = code == 0
}