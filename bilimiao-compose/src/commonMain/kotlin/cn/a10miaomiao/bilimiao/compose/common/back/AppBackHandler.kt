package cn.a10miaomiao.bilimiao.compose.common.back

import cn.a10miaomiao.bilimiao.compose.StartViewState
import cn.a10miaomiao.bilimiao.compose.base.BottomSheetState
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster

/**
 * 应用级返回处理
 *
 * 「返回」拆成三段，各平台按自己的原生机制组合，语义只有这一份实现：
 * 1. [handleOverlayBack]：覆盖层（BottomSheet / 全屏播放 / 搜索框 / 抽屉）
 * 2. 页面级返回处理器：Android 由系统 `OnBackPressedDispatcher` 分发，
 *    其它平台由应用返回栈 [BackDispatcher] 分发
 * 3. [handleNavigationBack]：页面导航后退，直至首页后两次返回退出
 *
 * Android：系统返回 → [handleOverlayBack] → 系统分发器 → [handleNavigationBack]
 * 桌面端等无系统返回的端：[handleBack] 依次跑完三段
 *
 * @param onExitApp 退出应用（Android: finish、桌面端: exitApplication）
 */
class AppBackHandler(
    private val backDispatcher: BackDispatcher,
    private val bottomSheetState: BottomSheetState,
    private val startViewState: StartViewState,
    private val playerDelegate: BasePlayerDelegate,
    private val pageNavigation: PageNavigation,
    private val onExitApp: () -> Unit,
) {

    private var lastExitPlayerTime = 0L
    private var lastExitAppTime = 0L

    /**
     * 覆盖层返回
     *
     * 优先级：BottomSheet 内部导航 → 退出全屏播放 → 搜索框 → 抽屉
     *
     * @return 是否被覆盖层消费
     */
    fun handleOverlayBack(): Boolean {
        // BottomSheet 打开：先处理内部页面导航，直至最后一级关闭 BottomSheet
        if (bottomSheetState.page.value != null) {
            if (!bottomSheetState.popInnerPage()) {
                bottomSheetState.close()
            }
            return true
        }
        // 全屏播放：退出全屏（播放器保持小窗播放，不关闭播放器）
        if (playerDelegate.fullscreenController.isFullscreen.value) {
            playerDelegate.fullscreenController.smallScreen()
            return true
        }
        // 搜索对话框 / 抽屉
        if (startViewState.showSearchDialog) {
            startViewState.closeSearchDialog()
            return true
        }
        if (startViewState.isDrawerOpen()) {
            startViewState.closeDrawer()
            return true
        }
        return false
    }

    /**
     * 收敛所有覆盖层：BottomSheet（连同内部页面）、全屏播放、搜索框、抽屉，全部关闭。
     * 供「返回首页 / 退出播放」这类一次性跳转的动作使用——直接改导航会让浮层留在新页面之上。
     *
     * 与 [handleOverlayBack] 的区别：后者按优先级只消费一层；本方法不关心优先级，一次全关。
     * 各项条件都是「当前是否处于该覆盖层」的读-判-关，已关闭时判空跳过，
     * 因此重复调用不会产生额外副作用（幂等），返回值只在本次真正关掉了东西时为 true。
     *
     * @return 关闭过程中是否有任何覆盖层被处理
     */
    fun closeOverlays(): Boolean {
        var handled = false
        // BottomSheet：先逐级弹空内部页面（popInnerPage 返回“是否仍有内部页”，弹空后为 false），
        // 再关闭 sheet 本身；没有内部页时直接跳过，不会死循环
        if (bottomSheetState.hasInnerPage) {
            while (bottomSheetState.popInnerPage()) {
                // 逐级弹出内部页面，直到只剩初始页
            }
            handled = true
        }
        if (bottomSheetState.page.value != null) {
            bottomSheetState.close()
            handled = true
        }
        // 全屏播放：退出全屏（播放器保持小窗播放，不关闭播放器）
        if (playerDelegate.fullscreenController.isFullscreen.value) {
            playerDelegate.fullscreenController.smallScreen()
            handled = true
        }
        if (startViewState.showSearchDialog) {
            startViewState.closeSearchDialog()
            handled = true
        }
        if (startViewState.isDrawerOpen()) {
            startViewState.closeDrawer()
            handled = true
        }
        return handled
    }

    /**
     * 导航返回与退出
     *
     * 页面导航后退直至首页；已在首页且 [allowExit] 时进入两次返回退出。
     *
     * @param allowExit 无路可退时是否进入两次返回退出流程
     * @return 是否已消费；false 只会在 [allowExit] 为 false 且无路可退时返回
     */
    fun handleNavigationBack(allowExit: Boolean = true): Boolean {
        // 页面导航后退，直至首页
        if (pageNavigation.canPopBackStack()) {
            pageNavigation.popBackStack()
            return true
        }
        if (!allowExit) return false
        // 首页：两次返回退出播放 / 退出应用
        if (playerDelegate.isOpened()) {
            handleExitPlayerBackPressed()
        } else {
            handleExitAppBackPressed()
        }
        return true
    }

    /**
     * 完整返回链路：覆盖层 → 应用返回栈 → 导航后退/退出
     *
     * 供没有系统返回通道的平台使用（桌面端 app bar 返回按钮、Esc）。
     *
     * @param allowExit 无路可退时是否进入两次返回退出流程。
     *        快捷键入口传 false：没有可返回的内容时不消费按键，避免抢占普通按键语义。
     * @return 是否已消费
     */
    fun handleBack(allowExit: Boolean = true): Boolean {
        if (handleOverlayBack()) return true
        if (backDispatcher.dispatch()) return true
        return handleNavigationBack(allowExit)
    }

    /**
     * 两次返回退出播放：第一次提示，第二次关闭播放器
     */
    private fun handleExitPlayerBackPressed() {
        val now = System.currentTimeMillis()
        if (now - lastExitPlayerTime > EXIT_CONFIRM_INTERVAL) {
            GlobalToaster.show("再按一次退出播放")
            lastExitPlayerTime = now
        } else {
            lastExitPlayerTime = 0
            playerDelegate.closePlayer()
        }
    }

    /**
     * 两次返回退出应用：第一次提示，第二次退出
     */
    private fun handleExitAppBackPressed() {
        val now = System.currentTimeMillis()
        if (now - lastExitAppTime > EXIT_CONFIRM_INTERVAL) {
            GlobalToaster.show("再按一次退出bilimiao")
            lastExitAppTime = now
        } else {
            lastExitAppTime = 0
            onExitApp()
        }
    }

    companion object {
        /** 两次返回确认的时间间隔（毫秒） */
        private const val EXIT_CONFIRM_INTERVAL = 2000L
    }
}
