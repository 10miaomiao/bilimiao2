package cn.a10miaomiao.bilimiao.compose.pages.setting.content

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.components.preference.sliderIntPreference
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.map
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.sliderPreference
import me.zhanghai.compose.preference.switchPreference
import org.kodein.di.compose.rememberInstance

@Composable
internal fun DanmakuDisplaySettingContent(
    danmakuPreferences: SettingPreferences.Danmaku,
) {
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val context = LocalContext.current
    val dataStore = remember {
        SettingPreferences.run { context.dataStore }
    }

    val enableSetting = if (danmakuPreferences.name != "default") {
        dataStore.data.map {
            it[danmakuPreferences.enable] ?: false
        }.collectAsState(initial = true).value
    } else {
        true
    }

    ProvidePreferenceLocals(
        flow = rememberPreferenceFlow(dataStore)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (danmakuPreferences.name != "default") {
                switchPreference(
                    key = danmakuPreferences.enable.name,
                    title = {
                        Text(text = "启用独立设置")
                    },
                    defaultValue = false,
                )
            }
            preferenceCategory(
                key = "display",
                title = {
                    Text(text = "显示")
                }
            )
            switchPreference(
                key = danmakuPreferences.show.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "显示弹幕")
                },
                defaultValue = true,
            )
            switchPreference(
                key = danmakuPreferences.r2lShow.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "滚动弹幕显示")
                },
                defaultValue = true,
            )
            switchPreference(
                key = danmakuPreferences.ftShow.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "顶部弹幕显示")
                },
                defaultValue = true,
            )
            switchPreference(
                key = danmakuPreferences.fbShow.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "底部弹幕显示")
                },
                defaultValue = true,
            )
            switchPreference(
                key = danmakuPreferences.specialShow.name,
                enabled = {
                    enableSetting
                },
                title = {
                    Text(text = "高级弹幕显示")
                },
                defaultValue = true,
            )
            // 滚动弹幕最大行数
            sliderIntPreference(
                key = danmakuPreferences.r2lMaxLine.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 0,
                title = { Text(text = "滚动弹幕最大行数") },
                valueRange = 0..20,
                valueSteps = 19,
                valueText = {
                    if (it == 0) {
                        Text(text = "无限制")
                    } else {
                        Text(text = "%d行".format(it))
                    }
                }
            )
            // 顶部弹幕最大行数
            sliderIntPreference(
                key = danmakuPreferences.ftMaxLine.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 0,
                title = { Text(text = "顶部弹幕最大行数") },
                valueRange = 0..20,
                valueSteps = 19,
                valueText = {
                    if (it == 0) {
                        Text(text = "无限制")
                    } else {
                        Text(text = "%d行".format(it))
                    }
                }
            )
            // 底部弹幕最大行数
            sliderIntPreference(
                key = danmakuPreferences.fbMaxLine.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 0,
                title = { Text(text = "底部弹幕最大行数") },
                valueRange = 0..20,
                valueSteps = 19,
                valueText = {
                    if (it == 0) {
                        Text(text = "无限制")
                    } else {
                        Text(text = "%d行".format(it))
                    }
                }
            )

            preferenceCategory(
                key = "font",
                title = {
                    Text(text = "字体")
                }
            )
            // 字体大小
            sliderPreference(
                key = danmakuPreferences.fontSize.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 1f,
                title = { Text(text = "字体大小") },
                valueRange = 0.1f..4f,
                valueSteps = 24,
                valueText = {
                    Text(text = "%.1f倍".format(it))
                }
            )
            // 不透明度
            sliderPreference(
                key = danmakuPreferences.opacity.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 1f,
                title = { Text(text = "字体不透明度") },
                valueRange = 0f..1f,
                valueSteps = 99,
                valueText = {
                    Text(text = "${(it * 100).toInt()}%")
                }
            )

            preferenceCategory(
                key = "speed",
                title = {
                    Text(text = "速度")
                }
            )
            // 弹幕速度
            sliderPreference(
                key = danmakuPreferences.speed.name,
                enabled = {
                    enableSetting
                },
                defaultValue = 1f,
                title = { Text(text = "弹幕速度") },
                valueRange = 0.1f..2f,
                valueSteps = 18,
                valueText = {
                    Text(text = "%.1f倍".format(it))
                }
            )

            item("bottom") {
                Spacer(
                    modifier = Modifier.height(
                        windowInsets.bottomDp.dp + windowStore.bottomAppBarHeightDp.dp
                    )
                )
            }
        }
    }
}