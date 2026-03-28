package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance

@Serializable
class AutoStopTimerPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: AutoStopTimerPageViewModel = diViewModel()
        AutoStopTimerPageContent(viewModel)
    }
}

private class AutoStopTimerPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val playerStore: PlayerStore by di.instance()

    val autoStopDurationFlow = playerStore.autoStopDurationFlow

    fun setAutoStopDuration(duration: Int) {
        playerStore.setAutoStopDuration(duration)
    }
}


@Composable
private fun AutoStopTimerPageContent(
    viewModel: AutoStopTimerPageViewModel
) {
    PageConfig(
        title = "定时关闭"
    )

    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val autoStopDuration by viewModel.autoStopDurationFlow.collectAsState()
    var sliderValue by remember(autoStopDuration) { mutableFloatStateOf(autoStopDuration.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = windowInsets.leftDp.dp,
                end = windowInsets.rightDp.dp,
                top = windowInsets.topDp.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // 当前设置标签
        Text(
            text = "当前设置",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 当前值显示
        Text(
            text = formatDuration(autoStopDuration),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 滑块
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = {
                viewModel.setAutoStopDuration(sliderValue.toInt())
            },
            valueRange = 0f..3600f,
            steps = 0,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        // 滑块说明
        Text(
            text = "0 - 60 分钟",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 快捷按钮区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickDurationButton(
                text = "关闭",
                onClick = { viewModel.setAutoStopDuration(0) }
            )
            QuickDurationButton(
                text = "15分钟",
                onClick = { viewModel.setAutoStopDuration(15 * 60) }
            )
            QuickDurationButton(
                text = "30分钟",
                onClick = { viewModel.setAutoStopDuration(30 * 60) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickDurationButton(
                text = "45分钟",
                onClick = { viewModel.setAutoStopDuration(45 * 60) }
            )
            QuickDurationButton(
                text = "1小时",
                onClick = { viewModel.setAutoStopDuration(60 * 60) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 提示文字
        Text(
            text = "定时关闭会在视频播放达到指定时长后自动暂停",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun QuickDurationButton(
    text: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier.width(100.dp)
    ) {
        Text(text = text)
    }
}

private fun formatDuration(seconds: Int): String {
    return if (seconds == 0) {
        "已关闭"
    } else {
        val minute = seconds / 60
        val second = seconds % 60
        if (minute == 0) {
            "${second}秒"
        } else if (second == 0) {
            "${minute}分钟"
        } else {
            "${minute}分${second}秒"
        }
    }
}