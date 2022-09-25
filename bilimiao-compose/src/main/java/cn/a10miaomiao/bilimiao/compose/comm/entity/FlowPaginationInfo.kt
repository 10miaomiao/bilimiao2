package cn.a10miaomiao.bilimiao.compose.comm.entity

import kotlinx.coroutines.flow.MutableStateFlow

class FlowPaginationInfo<T> {
    var loading = MutableStateFlow(false)
    var finished = MutableStateFlow(false)
    var pageNum = 1
    var pageSize = 20
    var data = MutableStateFlow(emptyList<T>())
    var fail = MutableStateFlow("")
}