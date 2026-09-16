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
import cn.a10miaomiao.bilimiao.danmaku.util.DanmakuUtils
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.LocalDanmakuInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * 本地发送弹幕的边框色（白色），用于与其它弹幕区分
 */
private const val LOCAL_DANMAKU_BORDER_COLOR = 0xFFFFFFFF.toInt()

/**
 * 本地发送弹幕的文字描边色（黑色）
 *
 * 与解析弹幕保持一致（见 [cn.a10miaomiao.bilimiao.danmaku.parser.BiliDanmakuParser]），
 * 否则白色文字在浅色画面上看不清。
 */
private const val LOCAL_DANMAKU_SHADOW_COLOR = 0xFF000000.toInt()

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
 * @param isPlaying 是否正在播放。弹幕引擎由该状态驱动启停：只有视频真正开始播放
 *                  （缓冲结束）后才推进弹幕时间，缓冲/暂停期间弹幕冻结，
 *                  避免首次缓冲耗时较长时弹幕先于视频画面播放
 * @param danmakuParser 弹幕解析器
 * @param localDanmakuFlow 本地发送成功的弹幕（见 [LocalDanmakuInfo]），加入引擎并以边框区分
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
    localDanmakuFlow: Flow<LocalDanmakuInfo> = emptyFlow(),
    modeName: String = "default",
    visible: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val engine = remember { mutableStateOf<DanmakuEngine?>(null) }
    val displayer = remember { mutableStateOf<IDisplayer?>(null) }
    // 视频是否已真正开始播放（缓冲结束后的首帧）：在此之前不渲染弹幕，
    // 避免首次缓冲耗时较长时 0ms 附近的弹幕（顶部/底部固定弹幕）先于画面出现。
    // 弹幕解析器变化（切换视频/分段）时重置。
    var playbackStarted by remember(danmakuParser) { mutableStateOf(false) }
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
        // 弹幕数据准备完成时不启动引擎（引擎启动时机由播放状态驱动，见下方同步逻辑），
        // 否则首次缓冲期间引擎内部时钟就开始推进，弹幕会先于视频画面滚动。
        eng.prepare()

        displayer.value = disp
        engine.value = eng
    }

    // 本地发送成功的弹幕：加入引擎并以边框区分于其它弹幕
    // （对齐旧版 bbmiao PlayerDelegate2.sendDanmaku）
    LaunchedEffect(engine.value) {
        val eng = engine.value ?: return@LaunchedEffect
        val context = eng.getConfig() ?: return@LaunchedEffect
        // 字号换算与解析弹幕保持一致（见 BiliDanmakuParser: fontSize * (density - 0.6f)）。
        // 密度必须取引擎显示器的 density，而不是 Compose 的 LocalDensity：
        // 桌面端显示器对解析密度设有下限（max(_density, 1.5f)），直接用 LocalDensity 会让字号偏小。
        val textSizeFactor = context.mDisplayer.density - 0.6f
        localDanmakuFlow.collect { info ->
            val item = context.mDanmakuFactory.createDanmaku(info.type, context) ?: return@collect
            DanmakuUtils.fillText(item, info.text)
            item.textSize = info.textSize * textSizeFactor
            item.textColor = info.textColor or 0xFF000000.toInt()
            // 与解析弹幕一致的黑色描边，保证浅色画面上可读
            item.textShadowColor = LOCAL_DANMAKU_SHADOW_COLOR
            item.setTime(info.position)
            item.borderColor = LOCAL_DANMAKU_BORDER_COLOR
            eng.addDanmaku(item)
        }
    }

    // 同步播放位置和播放/暂停状态。
    // 引擎的启停由播放状态驱动（数据准备完成时不启动，见上方 prepare 注释）：
    // 首次缓冲期间视频尚未开始播放，此时不启动引擎，弹幕不会先于画面滚动；
    // 缓冲结束开始播放后启动引擎并按播放位置同步，暂停/缓冲时冻结弹幕。
    LaunchedEffect(engine.value, isPlaying, currentPosition) {
        val eng = engine.value ?: return@LaunchedEffect
        if (isPlaying) {
            playbackStarted = true
            if (eng.isStop()) {
                eng.resume()
            }
            eng.externalPlayerPosition = currentPosition
        } else if (!eng.isStop()) {
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
    // playbackStarted：视频开始播放前不绘制弹幕（引擎停在 0ms，避免首屏弹幕提前出现）
    if (currentEngine != null && currentDisplayer != null && playbackStarted && visible && settingsVisible) {
        DanmakuCanvas(
            engine = currentEngine,
            displayer = currentDisplayer,
            frameTick = frameTick,
            modifier = modifier,
        )
    }
}
