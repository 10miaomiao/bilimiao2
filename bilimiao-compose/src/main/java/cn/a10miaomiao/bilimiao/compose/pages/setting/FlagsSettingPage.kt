package cn.a10miaomiao.bilimiao.compose.pages.setting

import android.content.Intent
import android.preference.PreferenceManager
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.components.preference.sliderIntPreference
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class FlagsSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: FlagsSettingPageViewModel = diViewModel()
        FlagsSettingPageContent(viewModel)
    }
}

private class FlagsSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

    val isShowRebootAppDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val ctx = fragment.requireContext()
            val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
            SettingPreferences.run {
                ctx.dataStore.data.map {
                    it[FlagSubContentShow] ?: false
                }.distinctUntilChanged()
            }.drop(1).collect {
                showRebootAppDialog()
                // DataStore无法同步读取，故另存一份到SharedPreferences
                prefs.edit {
                    putBoolean(SettingConstants.FLAGS_SUB_CONTENT_SHOW, it)
                }
            }
        }
    }

    fun rebootApp() {
        val ctx = fragment.requireContext()
        val packageManager = ctx.packageManager
        val intent = packageManager.getLaunchIntentForPackage(ctx.packageName)!!
        val componentName = intent.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        ctx.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    fun showRebootAppDialog() {
        isShowRebootAppDialog.value = true
    }

    fun hideRebootAppDialog() {
        isShowRebootAppDialog.value = false
    }
}


@Composable
private fun FlagsSettingPageContent(
    viewModel: FlagsSettingPageViewModel
) {
    PageConfig(
        title = "实验性功能"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val context = LocalContext.current
    val dataStore = remember {
        SettingPreferences.run { context.dataStore }
    }

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
                key = "experiments",
                title = {
                    Text("实验性功能")
                }
            )
            switchPreference(
                key = SettingPreferences.FlagSubContentShow.name,
                title = {
                    Text("横屏模式下双屏显示")
                },
                summary = {
                    Text("修改后需重启APP")
                },
                defaultValue = false,
            )

            sliderIntPreference(
                key = SettingPreferences.FlagContentSplit.name,
                title = {
                    Text("横屏模式下双屏内容分割比")
                },
                defaultValue = 35,
                valueRange = 0..100,
                valueText = {
                    Text(text = "$it ：${100 - it}")
                },
            )

            sliderIntPreference(
                key = SettingPreferences.FlagContentAnimationDuration.name,
                title = {
                    Text("内容区域动画时长")
                },
                summary = {
                    Text("为0时不显示动画")
                },
                defaultValue = 0,
                valueRange = 0..1000,
                valueText = {
                    Text(text = "${it}ms")
                },
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

    val isShowRebootAppDialog by viewModel.isShowRebootAppDialog.collectAsState()

    if (isShowRebootAppDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideRebootAppDialog,
            title = {
                Text(text = "提示")
            },
            text = {
                Text(text = "修改双屏显示，需重新打开APP后生效")
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::rebootApp
                ) {
                    Text(text = "立即重新打开")
                }
                TextButton(
                    onClick = viewModel::hideRebootAppDialog
                ) {
                    Text(text = "稍后手动")
                }
            }
        )
    }
}