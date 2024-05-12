package cn.a10miaomiao.bilimiao.compose.comm.entity

import kotlinx.coroutines.flow.MutableStateFlow

class FlowPaginationInfo<T>(
    val pageSize: Int = 20
) {
    var loading = MutableStateFlow(false)
    var finished = MutableStateFlow(false)
    var pageNum = 1
    var data = MutableStateFlow(emptyList<T>())
    var fail = MutableStateFlow("")
}