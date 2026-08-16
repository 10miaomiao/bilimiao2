package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import cn.a10miaomiao.bilimiao.danmaku.cache.SimpleTextCacheStuffer
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.platform.createPlatformDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine
import androidx.compose.runtime.rememberCoroutineScope

/**
 * 跨平台弹幕渲染覆盖层
 *
 * 在 Compose Draw 阶段将弹幕渲染到 Canvas，由平台 Displayer 实现：
 * - 安卓：[cn.a10miaomiao.bilimiao.danmaku.platform.AndroidDisplayer] (android.graphics.Canvas)
 * - 桌面：[cn.a10miaomiao.bilimiao.danmaku.platform.SkiaDisplayer] (Skia Canvas)
 *
 * 弹幕引擎 [DanmakuEngine] 在 commonMain，两端共用渲染逻辑。
 * 每帧由 [withFrameNanos] 驱动重绘，与视频播放器 Canvas 同步。
 *
 * @param currentPosition 当前播放位置（毫秒）
 * @param isPlaying 是否正在播放
 * @param danmakuParser 弹幕解析器
 * @param visible 弹幕是否可见
 * @param modifier 布局修饰符
 */
@Composable
fun DanmakuOverlay(
    currentPosition: Long,
    isPlaying: Boolean,
    danmakuParser: BaseDanmakuParser?,
    visible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val engine = remember { mutableStateOf<DanmakuEngine?>(null) }
    val displayer = remember { mutableStateOf<IDisplayer?>(null) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    // 初始化引擎
    LaunchedEffect(danmakuParser) {
        val parser = danmakuParser ?: return@LaunchedEffect
        val context = DanmakuContext.create()
        val disp = createPlatformDisplayer(context)
        disp.setDensities(density.density, (density.density * 160).toInt(), density.density)
        context.mDisplayer = disp
        context.setCacheStuffer(SimpleTextCacheStuffer(), null)

        val eng = DanmakuEngine(scope)
        eng.nonBlockModeEnable = true
        eng.idleSleep = false
        eng.setConfig(context)
        eng.setParser(parser)
        eng.setCallback(object : DanmakuEngine.Callback {
            override fun prepared() {
                eng.start()
            }
        })
        eng.prepare()

        displayer.value = disp
        engine.value = eng
    }

    // 同步播放位置和暂停状态
    LaunchedEffect(currentPosition, isPlaying) {
        val eng = engine.value ?: return@LaunchedEffect
        if (isPlaying) {
            if (eng.isStop()) {
                eng.resume()
            }
            eng.externalPlayerPosition = currentPosition
        } else {
            eng.pause()
        }
    }

    // 释放引擎
    DisposableEffect(Unit) {
        onDispose {
            engine.value?.release()
        }
    }

    // 帧驱动：每帧请求重绘
    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { frameTick++ }
        }
    }

    val currentEngine = engine.value
    val currentDisplayer = displayer.value
    if (currentEngine != null && currentDisplayer != null && visible) {
        PlatformDanmakuCanvas(
            engine = currentEngine,
            displayer = currentDisplayer,
            frameTick = frameTick,
            modifier = modifier,
        )
    }
}
