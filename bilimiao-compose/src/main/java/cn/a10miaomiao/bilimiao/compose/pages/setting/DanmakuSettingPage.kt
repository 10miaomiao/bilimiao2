package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class DanmakuSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: DanmakuSettingPageViewModel = diViewModel()
        DanmakuSettingPageContent(viewModel)
    }
}

private class DanmakuSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()


    private fun toDisplaySettingPage(
        name: String,
    ) {
        pageNavigation.navigate(DanmakuDisplaySettingPage(
            name = name
        ))
    }

    fun defaultDisplayClick() {
        toDisplaySettingPage(
            SettingPreferences.DanmakuDefault.name
        )
    }
    fun smallModeDisplayClick() {
        toDisplaySettingPage(
            SettingPreferences.DanmakuSmallMode.name
        )
    }
    fun fullModeDisplayClick() {
        toDisplaySettingPage(
            SettingPreferences.DanmakuFullMode.name
        )
    }
    fun pipModeDisplayClick() {
        toDisplaySettingPage(
            SettingPreferences.DanmakuPipMode.name
        )
    }
}


@Composable
private fun DanmakuSettingPageContent(
    viewModel: DanmakuSettingPageViewModel
) {
    PageConfig(
        title = "弹幕设置"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val context = LocalContext.current
    val dataStore = remember {
        SettingPreferences.run { context.dataStore }
    }
    val danmakuEnableArr by dataStore.data.map {
        arrayOf(
            it[SettingPreferences.DanmakuEnable] ?: true,
            it[SettingPreferences.DanmakuSmallMode.enable] ?: false,
            it[SettingPreferences.DanmakuFullMode.enable] ?: false,
            it[SettingPreferences.DanmakuPipMode.enable] ?: false,
        )
    }.collectAsState(initial = arrayOf(true, false, false, false))
    val danmakuEnable = danmakuEnableArr[0]

    ProvidePreferenceLocals(
        flow = rememberPreferenceFlow(dataStore)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                )
        ) {
            item("top") {
                Spacer(
                    modifier = Modifier.height(windowInsets.topDp.dp)
                )
            }
            preferenceCategory(
                key = "0",
                title = {
                    Text("基础设置")
                }
            )
            switchPreference(
                key = SettingPreferences.DanmakuEnable.name,
                title = {
                    Text("启用弹幕")
                },
                summary = {
                    if (it) {
                        Text("已启用")
                    } else {
                        Text("未启用，启用后才能进行其它设置")
                    }
                },
                defaultValue = true,
            )
            switchPreference(
                key = SettingPreferences.DanmakuSysFont.name,
                enabled = {
                    danmakuEnable
                },
                title = {
                    Text("弹幕使用系统字体")
                },
                summary = {
                    Text("修改后需重启APP生效")
                },
                defaultValue = true,
            )

            preferenceCategory(
                key = "1",
                title = {
                    Text("显示设置")
                }
            )
            preference(
                key = "默认",
                enabled = danmakuEnable,
                title = {
                    Text("默认显示设置")
                },
                summary = {
                    Text("未独立设置时，使用默认设置")
                },
                onClick = viewModel::defaultDisplayClick
            )
            preference(
                key = "小屏模式",
                enabled = danmakuEnable,
                title = {
                    Text("小屏模式显示设置")
                },
                summary = {
                      if (danmakuEnableArr[1]) {
                          Text("已启用独立设置")
                      } else {
                          Text("未启用独立设置，使用默认设置")
                      }
                },
                onClick = viewModel::smallModeDisplayClick
            )
            preference(
                key = "全屏模式",
                enabled = danmakuEnable,
                title = {
                    Text("全屏模式显示设置")
                },
                summary = {
                    if (danmakuEnableArr[2]) {
                        Text("已启用独立设置")
                    } else {
                        Text("未启用独立设置，使用默认设置")
                    }
                },
                onClick = viewModel::fullModeDisplayClick
            )
            preference(
                key = "小窗模式",
                enabled = danmakuEnable,
                title = {
                    Text("小窗(画中画)模式显示设置")
                },
                summary = {
                    if (danmakuEnableArr[3]) {
                        Text("已启用独立设置")
                    } else {
                        Text("未启用独立设置，使用默认设置")
                    }
                },
                onClick = viewModel::pipModeDisplayClick
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
        