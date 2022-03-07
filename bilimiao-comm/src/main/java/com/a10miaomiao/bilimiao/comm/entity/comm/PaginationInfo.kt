package com.a10miaomiao.bilimiao.comm.entity.comm

data class PaginationInfo <T>(
    var loading: Boolean = false,
    var finished: Boolean = false,
    var pageNum: Int = 1,
    var pageSize: Int = 20,
    var data: ArrayList<T> = arrayListOf(),
    var fail: Boolean = false,
)