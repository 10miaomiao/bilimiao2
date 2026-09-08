package cn.a10miaomiao.bilimiao.compose.components.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import cn.a10miaomiao.bilimiao.danmaku.cache.SimpleTextCacheStuffer
import cn.a10miaomiao.bilimiao.danmaku.context.DanmakuContext
import cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku
import cn.a10miaomiao.bilimiao.danmaku.model.IDisplayer
import cn.a10miaomiao.bilimiao.danmaku.parser.BaseDanmakuParser
import cn.a10miaomiao.bilimiao.danmaku.platform.createPlatformDisplayer
import cn.a10miaomiao.bilimiao.danmaku.task.DanmakuEngine
import cn.a10miaomiao.bilimiao.danmaku.ui.DanmakuCanvas
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore

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
 * 弹幕显示设置（字号、透明度、速度、显示类型、最大行数等）订阅
 * [appDataStore]，变化时实时应用到引擎，对齐旧版
 * `PlayerController.initDanmakuContext` 行为。
 *
 * @param currentPosition 当前播放位置（毫秒）
 * @param isPlaying 是否正在播放
 * @param danmakuParser 弹幕解析器
 * @param modeName 当前播放模式（[SettingPreferences.Danmaku] 的 name），
 *                 用于读取对应模式的弹幕设置（small/full/pip，默认 default）
 * @param visible 弹幕是否可见（播放器按钮开关）
 * @param modifier 布局修饰符
 */
@Composable
fun DanmakuOverlay(
    currentPosition: Long,
    isPlaying: Boolean,
    danmakuParser: BaseDanmakuParser?,
    modeName: String = "default",
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

    // 弹幕设置中当前模式的"显示弹幕"开关（与播放器按钮开关 AND 后决定最终可见性）
    var settingsVisible by remember { mutableStateOf(true) }

    // 订阅弹幕设置变化，实时应用到弹幕引擎（对齐旧版 PlayerController.initDanmakuContext）
    LaunchedEffect(engine.value, modeName) {
        val eng = engine.value ?: return@LaunchedEffect
        val context = eng.getConfig() ?: return@LaunchedEffect
        val danmakuMode = when (modeName) {
            SettingPreferences.DanmakuSmallMode.name -> SettingPreferences.DanmakuSmallMode
            SettingPreferences.DanmakuFullMode.name -> SettingPreferences.DanmakuFullMode
            SettingPreferences.DanmakuPipMode.name -> SettingPreferences.DanmakuPipMode
            else -> SettingPreferences.DanmakuDefault
        }
        appDataStore.data.collect { preferences ->
            // 未启用该模式的独立设置时，回退到默认模式配置
            val mode = if (preferences[danmakuMode.enable] == true) {
                danmakuMode
            } else {
                SettingPreferences.DanmakuDefault
            }
            // 弹幕显示 = 全局开关 && 当前模式"显示弹幕"
            settingsVisible = (preferences[SettingPreferences.DanmakuEnable] ?: true) &&
                (preferences[mode.show] ?: true)

            // 弹幕样式与显示类型（对齐旧版 initDanmakuContext）
            context.setFTDanmakuVisibility(preferences[mode.ftShow] ?: true)
            context.setFBDanmakuVisibility(preferences[mode.fbShow] ?: true)
            context.setR2LDanmakuVisibility(preferences[mode.r2lShow] ?: true)
            context.setSpecialDanmakuVisibility(preferences[mode.specialShow] ?: true)
            // 速度设置越大，滚动越快（滚动速度系数取其倒数）
            context.setScrollSpeedFactor(1f / (preferences[mode.speed] ?: 1f))
            context.setScaleTextSize(preferences[mode.fontSize] ?: 1f)
            context.setDanmakuTransparency(preferences[mode.opacity] ?: 1f)
            // 最大行数：0 或无设置为无限制，全部无限制时取消行数过滤
            val maxLines = buildMap {
                preferences[mode.r2lMaxLine]?.takeIf { it > 0 }
                    ?.let { put(BaseDanmaku.TYPE_SCROLL_RL, it) }
                preferences[mode.ftMaxLine]?.takeIf { it > 0 }
                    ?.let { put(BaseDanmaku.TYPE_FIX_TOP, it) }
                preferences[mode.fbMaxLine]?.takeIf { it > 0 }
                    ?.let { put(BaseDanmaku.TYPE_FIX_BOTTOM, it) }
            }
            context.setMaximumLines(maxLines.ifEmpty { null })
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
    if (currentEngine != null && currentDisplayer != null && visible && settingsVisible) {
        DanmakuCanvas(
            engine = currentEngine,
            displayer = currentDisplayer,
            frameTick = frameTick,
            modifier = modifier,
        )
    }
}
