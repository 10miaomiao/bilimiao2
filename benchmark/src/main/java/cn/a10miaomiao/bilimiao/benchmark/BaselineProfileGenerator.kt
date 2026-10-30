package cn.a10miaomiao.bilimiao.benchmark

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 采集 baseline profile 用。
 *
 * 迁移到 Compose 后，UiAutomator 的 `By.text` 无法命中 Compose 文本（旧实现里的
 * `clickOnText` 因此失效），所以改用深链直达页面：
 * 冷启动首页 → 首页列表滑动 → 视频详情 → 历史记录 → 设置页。
 *
 * 深链定义见 bilimiao-compose 的 BilimiaoDeepLinks.kt。
 */
@RequiresApi(Build.VERSION_CODES.P)
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalBaselineProfilesApi::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collectBaselineProfile(packageName = PACKAGE_NAME) {
            pressHome()
            startActivityAndWait() // 冷启动：首页
            device.waitForIdle(1_000)

            // 首页列表滑动，覆盖列表测量/绘制与图片加载
            device.swipe(
                device.displayWidth / 2,
                (device.displayHeight * 0.75f).toInt(),
                device.displayWidth / 2,
                (device.displayHeight * 0.30f).toInt(),
                20,
            )
            device.waitForIdle(1_000)

            // 视频详情：Compose 页面 + diViewModel 反射创建 + 播放器组件
            startActivityAndWait(deepLink("bilimiao://video/BV1GJ411x7h7"))
            device.waitForIdle(2_000)

            // 历史记录
            startActivityAndWait(deepLink("bilimiao://mine/history"))
            device.waitForIdle(2_000)

            // 设置页
            startActivityAndWait(deepLink("bilimiao://setting"))
            device.waitForIdle(1_000)
        }
    }

    private fun deepLink(url: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage(PACKAGE_NAME)
        }

    private companion object {
        const val PACKAGE_NAME = "com.a10miaomiao.bilimiao"
    }
}
