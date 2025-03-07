package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.flow.stateMap
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import cn.a10miaomiao.bilimiao.compose.pages.setting.components.ThemeColorButton
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences.dataStore
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class ThemeSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: ThemeSettingPageViewModel = diViewModel()
        ThemeSettingPageContent(viewModel)
    }

}

private class ThemeSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val appStore by instance<AppStore>()
    private val pageNavigation by instance<PageNavigation>()

    val darkModeList = listOf(
        0 to "跟随系统",
        1 to "关闭",
        2 to "打开"
    )
    val darkModeListSize get() = darkModeList.size

    val appBarTypeList = listOf(
        0 to "主题颜色",
        1 to "纯色",
    )
    val appBarTypeListSize get() = appBarTypeList.size

    val colorList = listOf<Pair<Long, String>>(
        0xFFFB7299 to "少女粉",
        0xFF2196F3 to "胖次蓝",
        0xFFFDD835 to "咸蛋黄",
        0xFFFF9800 to "咸蛋黄2",
        0xFF673AB7 to "紫色",
        0xFF4CAF50 to "早苗绿",
        0xFFF44336 to "姨妈红",
    )

    val themeState = appStore.stateFlow.stateMap {
        it.theme ?: AppStore.ThemeSettingState(
            color = 0xFFFB7299,
        )
    }

    fun setDarkMode(mode: Int) {
        appStore.setDarkMode(mode)
//        if (mode == 0) {
//            windowStore.setSystemUiMode()
//        } else {
//            windowStore.setDarkMode(mode == 2)
//        }
    }

    fun setAppBarType(type: Int) {
        appStore.setAppBarType(type)
    }

    fun setThemeColor(color: Long) {
        appStore.setThemeColor(color)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeSettingPageContent(
    viewModel: ThemeSettingPageViewModel
) {
    PageConfig(
        title = "主题设置"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())
    val themeState by viewModel.themeState.collectAsState()

    LazyColumn(
        contentPadding = windowInsets.toPaddingValues(),
    ) {
        item {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                )
            ) {
                Text(
                    text = "深色模式",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                    ),
                )
                SingleChoiceSegmentedButtonRow {
                    viewModel.darkModeList.forEachIndexed { index, mode ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = viewModel.darkModeListSize,
                            ),
                            onClick = {
                                viewModel.setDarkMode(mode.first)
                            },
                            selected = index == themeState.darkMode,
                            modifier = Modifier.width(IntrinsicSize.Max),
                        ) {
                            Text(
                                text = mode.second,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                )
            ) {
                Text(
                    text = "应用操作栏风格",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                    ),
                )
                SingleChoiceSegmentedButtonRow {
                    viewModel.appBarTypeList.forEachIndexed { index, type ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = viewModel.appBarTypeListSize,
                            ),
                            onClick = {
                                viewModel.setAppBarType(type.first)
                            },
                            selected = index == themeState.appBarType,
                            modifier = Modifier.width(IntrinsicSize.Max),
                        ) {
                            Text(
                                text = type.second,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                )
            ) {
                Text(
                    text = "主题颜色",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                    ),
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    viewModel.colorList.forEach { color ->
                        ThemeColorButton(
                            onClick = {
                                viewModel.setThemeColor(color.first)
                            },
                            baseColor = Color(color.first),
                            selected = themeState.color == color.first,
                            colorName = color.second,
                        )
                    }
                }
            }
        }

    }
}