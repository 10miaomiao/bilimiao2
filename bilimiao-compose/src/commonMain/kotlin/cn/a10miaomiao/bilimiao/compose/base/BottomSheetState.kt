package cn.a10miaomiao.bilimiao.compose.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * BottomSheet 状态。
 *
 * 持有 sheet 初始页（[page]）与 sheet 打开后的内部导航栈（[innerPages]）。
 * 内部导航栈独立于主 backstack：sheet 内部页面调用 navigate 时进入该栈，
 * 返回时先 pop 内部页面，直至最后一个内部页面关闭后再关闭整个 BottomSheet。
 */
class BottomSheetState {

    private val _page = MutableStateFlow<ComposePage?>(null)
    val page: StateFlow<ComposePage?> get() = _page

    /** sheet 内部导航栈（不含初始页，仅 sheet 打开后 navigate 进入的页面） */
    private val _innerPages = MutableStateFlow<List<ComposePage>>(emptyList())
    val innerPages: StateFlow<List<ComposePage>> get() = _innerPages

    /** 当前 sheet 显示页面：内部栈顶页，无内部页时为初始页 */
    val currentPage: ComposePage?
        get() = _innerPages.value.lastOrNull() ?: _page.value

    /** 是否存在 sheet 内部页面（决定返回时先处理内部导航） */
    val hasInnerPage: Boolean
        get() = _innerPages.value.isNotEmpty()

    fun open(page: ComposePage) {
        _page.value = page
        _innerPages.value = emptyList()
    }

    /**
     * sheet 内部导航：将页面压入内部栈。
     * 与主导航保持一致的单顶语义：目标与栈顶同类型时跳过。
     */
    fun navigate(page: ComposePage) {
        if (_page.value == null) return
        val pages = _innerPages.value
        if (pages.lastOrNull()?.let { it::class == page::class } != true) {
            _innerPages.value = pages + page
        }
    }

    /**
     * pop 内部栈顶页。
     * @return 是否仍有内部页面（true 表示还有内部页，false 表示已到初始页，应关闭 sheet）
     */
    fun popInnerPage(): Boolean {
        val pages = _innerPages.value
        if (pages.isEmpty()) return false
        _innerPages.value = pages.dropLast(1)
        return _innerPages.value.isNotEmpty()
    }

    fun close() {
        _page.value = null
        _innerPages.value = emptyList()
    }
}
