package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

const val TAG_BOTTOM_PROGRESS_BAR = "BottomProgressBar"

/**
 * 画面底部的细进度条（对齐旧版 `DanmakuVideoPlayer` 的 `mBottomProgressBar`）。
 *
 * 控制器隐藏时贴画面底边显示当前播放进度，不必唤出控制器也能看到播放到哪了；
 * 控制器显示时由调用方隐藏本组件（底栏内已有可拖动的进度滑块，避免重复）。
 *
 * 进度在绘制阶段读取，更新只触发重绘，不会重组播放器。
 * 传入 [smoothedProgress] 时用逐帧外推的平滑位置绘制（避免 200ms 采样造成的跳格）；
 * 拖动 / 横滑预览期间仍跟随滑块状态，保证与底栏显示一致。
 *
 * @param state 进度条状态，与底栏进度滑块共用同一实例，保证两处显示一致
 * @param smoothedProgress 逐帧平滑的播放位置（见 [rememberSmoothedProgressState]），
 * 为 `null` 时直接按 [PlayerProgressSliderState.displayPositionRatio] 绘制
 * @param thickness 进度条高度（旧版为 4dp 的圆角细条，这里取 3dp 适配高密度屏幕）
 * @param trackColor 轨道背景色（旧版为 30% 黑）
 * @param progressColor 已播放部分的颜色
 */
@Composable
fun BottomProgressBar(
    state: PlayerProgressSliderState,
    modifier: Modifier = Modifier,
    smoothedProgress: SmoothedProgressState? = null,
    thickness: Dp = 3.dp,
    trackColor: Color = Color.Black.copy(alpha = 0.3f),
    progressColor: Color = MaterialTheme.colorScheme.primary,
) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(thickness)
            .testTag(TAG_BOTTOM_PROGRESS_BAR),
    ) {
        drawRect(
            color = trackColor,
            topLeft = Offset.Zero,
            size = size,
        )
        val ratio = progressRatioOf(state, smoothedProgress)
        if (ratio > 0f) {
            drawRect(
                color = progressColor,
                topLeft = Offset.Zero,
                size = Size(width = size.width * ratio, height = size.height),
            )
        }
    }
}

/**
 * 计算要绘制的进度比例（0..1）。
 *
 * 拖动 / 横滑预览时以滑块状态为准（跟手，且与底栏一致），
 * 其余情况用平滑位置除以总时长，总时长未知时回退到滑块状态。
 */
private fun progressRatioOf(
    state: PlayerProgressSliderState,
    smoothedProgress: SmoothedProgressState?,
): Float {
    if (smoothedProgress == null || state.isPreviewing) {
        return state.displayPositionRatio.coerceIn(0f, 1f)
    }
    val duration = state.totalDurationMillis
    if (duration <= 0) {
        return state.displayPositionRatio.coerceIn(0f, 1f)
    }
    return (smoothedProgress.smoothedPositionMillis / duration.toFloat()).coerceIn(0f, 1f)
}
