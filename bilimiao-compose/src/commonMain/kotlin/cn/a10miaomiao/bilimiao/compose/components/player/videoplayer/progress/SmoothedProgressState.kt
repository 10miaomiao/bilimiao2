package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.progress

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.exp

/**
 * 播放进度的逐帧平滑状态（缓动进度条）。
 *
 * 播放位置来自轮询采样：`PlayerSession` 约 200ms 读一次播放器位置写进
 * `currentPositionFlow`，直接拿它画进度条会看到明显的「一跳一跳」。这里按帧时间外推：
 *
 * 1. 以最近一次真实采样为基准，按 `经过时间 × 倍速` 推算此刻应到的位置；
 * 2. 推算值与显示值之间的微小误差用指数收敛吃掉（与帧率无关），采样抖动不会让进度条抖动；
 * 3. 暂停 / 缓冲时不外推，原地收敛到采样值；
 * 4. seek、切源、重播等大跳变直接吸附，不做追赶动画（避免进度条横穿整条画面）。
 *
 * [smoothedPositionMillis] 只应在**绘制阶段**读取（例如 `Canvas` 的 onDraw），
 * Compose 会把这次读取记为绘制依赖，位置变化只触发重绘，不会触发重组。
 */
@Stable
class SmoothedProgressState(
    private val positionMillis: () -> Long,
    private val durationMillis: () -> Long,
    private val isPlaying: () -> Boolean,
    private val playbackSpeed: () -> Float,
) {
    /**
     * 平滑后的播放位置（毫秒）。逐帧更新，只应在绘制阶段读取。
     */
    var smoothedPositionMillis by mutableLongStateOf(0L)
        private set

    /** 最近一次真实采样值（外推基准） */
    private var basePositionMillis = Long.MIN_VALUE

    /** 最近一次真实采样发生时的帧时间（纳秒） */
    private var baseFrameNanos = 0L

    /** 上一帧的帧时间（纳秒），0 表示尚未绘制过 */
    private var lastFrameNanos = 0L

    internal fun onFrame(frameNanos: Long): Boolean {
        val rawPosition = positionMillis()
        val duration = durationMillis()
        val playing = isPlaying()

        // 真实采样变化（约 200ms 一次）：以它为新的外推基准
        if (rawPosition != basePositionMillis) {
            basePositionMillis = rawPosition
            baseFrameNanos = frameNanos
        }

        val current = if (lastFrameNanos == 0L) rawPosition.toFloat() else smoothedPositionMillis.toFloat()
        val predicted = if (playing) {
            val elapsedMillis = (frameNanos - baseFrameNanos) / NANOS_PER_MILLI
            basePositionMillis + elapsedMillis * playbackSpeed().coerceAtLeast(0f)
        } else {
            // 暂停 / 缓冲：不向前外推，只等采样值本身
            basePositionMillis.toFloat()
        }
        val target = if (duration > 0) {
            predicted.coerceIn(0f, duration.toFloat())
        } else {
            predicted.coerceAtLeast(0f)
        }

        val delta = target - current
        val next = when {
            // 首帧、明显跳变（seek / 切源 / 起播）与倒退：直接吸附，不做追赶动画
            lastFrameNanos == 0L || abs(delta) > SNAP_THRESHOLD_MILLIS -> target.toLong()
            else -> {
                val deltaMillis = (frameNanos - lastFrameNanos) / NANOS_PER_MILLI
                val alpha = 1f - exp(-deltaMillis / CATCH_UP_TIME_CONSTANT_MILLIS)
                (current + delta * alpha).toLong()
            }
        }
        lastFrameNanos = frameNanos
        // 暂停且已收敛时值不再变化，不写状态 => 不产生重绘
        if (next != smoothedPositionMillis) {
            smoothedPositionMillis = next
        }
        // 播放中或仍在收敛才需要逐帧刷新，否则交给调用方降频
        return playing || abs(target - smoothedPositionMillis) >= 1f
    }

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000f

        /** 显示值与推算值相差超过该值即视为跳变，直接吸附（毫秒） */
        const val SNAP_THRESHOLD_MILLIS = 1_000f

        /** 误差收敛的时间常数：越小追得越快、越容易看见采样抖动（毫秒） */
        const val CATCH_UP_TIME_CONSTANT_MILLIS = 150f
    }
}

/**
 * 创建并驱动一个逐帧平滑的播放进度状态。
 *
 * 循环策略：播放中（或误差尚未收敛）逐帧刷新；暂停且收敛后降频为低频唤醒，
 * 只用来捕捉 seek / 恢复播放这类跳变，避免暂停时白白以 60fps 空转。
 * 界面不可见时合成器不再发帧，循环自然停摆。
 *
 * @param positionMillis 真实播放位置（轮询采样，允许抖动）
 * @param durationMillis 总时长，用于把外推结果钳制在有效范围内
 * @param isPlaying 是否正在推进（缓冲 / 暂停时为 `false`，此时不做外推）
 * @param playbackSpeed 当前倍速，外推按它计算位置增速
 */
@Composable
fun rememberSmoothedProgressState(
    positionMillis: () -> Long,
    durationMillis: () -> Long,
    isPlaying: () -> Boolean,
    playbackSpeed: () -> Float,
): SmoothedProgressState {
    // 闭包每帧读取，用 rememberUpdatedState 保证读到的永远是最新值，且不会因此重启帧循环
    val latestPosition by rememberUpdatedState(positionMillis)
    val latestDuration by rememberUpdatedState(durationMillis)
    val latestIsPlaying by rememberUpdatedState(isPlaying)
    val latestSpeed by rememberUpdatedState(playbackSpeed)

    val state = remember {
        SmoothedProgressState(
            positionMillis = { latestPosition() },
            durationMillis = { latestDuration() },
            isPlaying = { latestIsPlaying() },
            playbackSpeed = { latestSpeed() },
        )
    }
    LaunchedEffect(state) {
        while (true) {
            var animating = true
            while (animating) {
                animating = withFrameNanos { frameNanos -> state.onFrame(frameNanos) }
            }
            // 已收敛（通常为暂停状态）：降频唤醒，等待 seek / 恢复播放等跳变
            delay(IDLE_REFRESH_INTERVAL_MILLIS)
        }
    }
    return state
}

/** 暂停且已收敛时的唤醒间隔，用于及时反映 seek 等跳变（毫秒） */
private const val IDLE_REFRESH_INTERVAL_MILLIS = 100L
