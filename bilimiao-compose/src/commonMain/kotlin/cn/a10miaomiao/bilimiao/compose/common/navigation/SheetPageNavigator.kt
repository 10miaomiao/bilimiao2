package cn.a10miaomiao.bilimiao.compose.common.navigation

import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.pages.video.VideoDetailPage

/**
 * BottomSheet 内部的 [PageNavigator]。
 *
 * sheet 内部页面的导航操作（navigate / popBackStack / navigateByUri）全部作用于
 * [BottomSheetState] 的内部导航栈，与主 backstack 相互独立：
 * - navigate：压入 sheet 内部栈
 * - popBackStack：pop sheet 内部栈（返回 false 表示已无内部页，此时应关闭 sheet）
 * - 浏览器/扫码等平台操作委托给父级 [PageNavigator]
 */
class SheetPageNavigator(
    private val sheetState: BottomSheetState,
    private val parentNavigator: PageNavigator,
) : PageNavigator {

    override fun <T : ComposePage> navigate(route: T) {
        sheetState.navigate(route)
    }

    override fun canPopBackStack(): Boolean {
        return sheetState.hasInnerPage
    }

    override fun popBackStack(): Boolean {
        return if (sheetState.hasInnerPage) {
            sheetState.popInnerPage()
            true
        } else {
            false
        }
    }

    override fun navigateByUri(uriString: String): Boolean {
        val page = BilibiliNavigation.resolveUri(uriString)
        return if (page != null) {
            sheetState.navigate(page)
            true
        } else {
            false
        }
    }

    override fun navigateToVideoInfo(id: String) {
        navigate(VideoDetailPage(id = id))
    }

    override fun launchWebBrowser(url: String) {
        parentNavigator.launchWebBrowser(url)
    }

    override fun openScanner(callback: (result: String) -> Unit): Boolean {
        return parentNavigator.openScanner(callback)
    }
}
