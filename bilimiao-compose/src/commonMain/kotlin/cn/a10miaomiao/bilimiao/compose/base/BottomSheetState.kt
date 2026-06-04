package cn.a10miaomiao.bilimiao.compose.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BottomSheetState {

    private val _page = MutableStateFlow<ComposePage?>(null)
    val page: StateFlow<ComposePage?> get() = _page

    fun open(page: ComposePage) {
        _page.value = page
    }

    fun close() {
        _page.value = null
    }


}