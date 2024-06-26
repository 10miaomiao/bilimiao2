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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.commponents.preference.sliderIntPreference
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.store.WindowStore
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class FlagsSettingPage : ComposePage() {
    override val route: String
        get() = "setting/flags"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel: FlagsSettingPageViewModel = diViewModel()
        FlagsSettingPageContent(viewModel)
    }
}

private class FlagsSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

}


@Composable
private fun FlagsSettingPageContent(
    viewModel: FlagsSettingPageViewModel
) {
    PageConfig(
        title = ""
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

//    val context = LocalContext.current
//    val dataStore = remember {
//        SettingPreferences.run { context.dataStore }
//    }

    ProvidePreferenceLocals() {
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
                key = SettingConstants.FLAGS_SUB_CONTENT_SHOW,
                title = {
                    Text("横屏模式下双屏显示")
                },
                summary = {
                    Text("修改后需重启APP")
                },
                defaultValue = false,
//                onChange = {
//                    showRebootAppDialog("修改双屏显示，需重新打开APP后生效")
//                }
            )

            sliderIntPreference(
                key = SettingConstants.FLAGS_CONTENT_DEFAULT_SPLIT,
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
                key = SettingConstants.FLAGS_CONTENT_ANIMATION_DURATION,
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
}