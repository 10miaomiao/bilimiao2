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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.comm.diViewModel
import cn.a10miaomiao.bilimiao.compose.comm.localContainerView
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.comm.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.commponents.preference.listStylePreference
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences.dataStore
import com.a10miaomiao.bilimiao.store.WindowStore
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preferenceCategory
import me.zhanghai.compose.preference.switchPreference
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

class VideoSettingPage : ComposePage() {
    override val route: String
        get() = "setting/video"

    @Composable
    override fun AnimatedContentScope.Content(navEntry: NavBackStackEntry) {
        val viewModel: VideoSettingPageViewModel = diViewModel()
        VideoSettingPageContent(viewModel)
    }
}

private class VideoSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

}


@Composable
private fun VideoSettingPageContent(
    viewModel: VideoSettingPageViewModel
) {
    PageConfig(
        title = "播放设置"
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
                key = "0",
                title = {
                    Text("播放器设置")
                }
            )
            switchPreference(
                key = SettingPreferences.PlayerBackground.name,
                title = {
                    Text("后台播放")
                },
                summary = {
                    Text("遇到困难时，不要停下来.")
                },
                defaultValue = true,
            )
            switchPreference(
                key = SettingPreferences.PlayerAudioFocus.name,
                title = {
                    Text("占用音频焦点")
                },
                summary = {
                    Text("关闭后可以与其它APP同时播放")
                },
                defaultValue = true,
            )

            preferenceCategory(
                key = "1",
                title = {
                    Text("视频源设置")
                }
            )

            item("bottom") {
                Spacer(
                    modifier = Modifier.height(windowInsets.bottomDp.dp)
                )
            }
        }
    }
}