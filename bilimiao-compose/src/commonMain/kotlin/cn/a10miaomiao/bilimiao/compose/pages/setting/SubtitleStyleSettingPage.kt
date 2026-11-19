package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import bilimiao.bilimiao_compose.generated.resources.Res
import bilimiao.bilimiao_compose.generated.resources.top_bg1
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.addPaddingValues
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.preference.ProvidePreferenceLocals
import cn.a10miaomiao.bilimiao.compose.common.preference.SliderPreference
import cn.a10miaomiao.bilimiao.compose.common.preference.preference
import cn.a10miaomiao.bilimiao.compose.common.preference.preferenceCategory
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceFlow
import cn.a10miaomiao.bilimiao.compose.common.preference.rememberPreferenceState
import cn.a10miaomiao.bilimiao.compose.components.layout.DoubleColumnAutofitLayout
import cn.a10miaomiao.bilimiao.compose.components.layout.chain_scrollable.rememberChainScrollableLayoutState
import cn.a10miaomiao.bilimiao.compose.components.player.SUBTITLE_FRAME_HORIZONTAL_PADDING
import cn.a10miaomiao.bilimiao.compose.components.player.SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT
import cn.a10miaomiao.bilimiao.compose.components.player.SubtitleStyleSpec
import cn.a10miaomiao.bilimiao.compose.components.player.SubtitleTextBox
import cn.a10miaomiao.bilimiao.compose.components.player.bottomMarginDescription
import cn.a10miaomiao.bilimiao.compose.components.player.bottomMarginFraction
import cn.a10miaomiao.bilimiao.compose.components.player.fontSizeDescription
import cn.a10miaomiao.bilimiao.compose.components.player.fontSizeFraction
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.datastore.appDataStore
import com.a10miaomiao.bilimiao.comm.datastore.editPreferences
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * 「字幕样式」独立设置页。
 *
 * 从「播放设置」的「字幕设置」进入，集中调整 CC 字幕的字号与距底部距离。页面用
 * [DoubleColumnAutofitLayout] 自适应排版：
 * - 单栏（窄屏）时，预览画面与紧贴其下方的提示行是可折叠的顶部 header；
 * - 双栏（宽窗）时，它们变成左侧一栏，右侧是设置项列表；
 * - 设置项（字号 / 位置 / 恢复默认）始终放在底部的 LazyColumn 里。
 *
 * 布局分两处：
 * - **提示行紧贴预览画面下方，同一行里左「距底部」、右「字号」**（如 `距底部 12dp` … `字号 16sp`），
 *   只显示**当前单位**的数值，不再同时列出换算出来的另一种单位，避免一处数字两套口径；
 * - **单位选择在各自滑杆的正下方**（字号滑杆 → 字号单位 chips、位置滑杆 → 位置单位 chips），
 *   选中后先改实时值（预览、滑杆、提示立刻跟着变）再落盘。
 *
 * 字号单位与位置单位**各自独立**（固定值 sp/dp 或按画面尺寸百分比），可以自由组合，
 * 换算规则由 [SubtitleStyleSpec] 统一提供：预览按 [fontSizeFraction] /
 * [bottomMarginFraction] 折算成比例渲染，与播放器百分比模式的渲染规则一致。其中
 * **字号百分比按画面短边**（`min(宽, 高)`，横屏竖屏观感一致）、**距底百分比按画面高度**，
 * 详见 [SubtitleStylePreviewCard]；字幕文本复用 [SubtitleTextBox]，排版、底衬、行高与播放器完全相同。
 *
 * 预览读的是**实时值**而不是持久化值：拖动滑杆过程中偏好尚未写回（zhanghai 的
 * [SliderPreference] 只在松手时回调 `onValueChange`），因此这里另外维护一份 `liveSpec`，
 * 让预览跟着手指走，松手后才落盘。
 */
@Serializable
class SubtitleStyleSettingPage : ComposePage {

    @Composable
    override fun Content() {
        PageConfig(
            title = "字幕样式"
        )
        SubtitleStyleSettingContent()
    }
}

/** 预览框里的示例字幕（长度足以体现居中与换行） */
private const val PREVIEW_SUBTITLE_TEXT = "这是一句示例字幕，用来检查字号大小与停靠位置是否合适。"

/**
 * 单栏布局时顶部 header 的高度，也是 header 可折叠的距离；同时决定单栏下预览的可用高度。
 *
 * 取值依据（360dp 宽屏、[PAGE_HORIZONTAL_PADDING] = 16dp）：
 * - 预览可用宽 328dp → 16:9 高 **184.5dp**；
 * - `Spacer` 8dp + 提示行约 **20dp**（labelMedium + 上 2dp 内边距）+ Column 上下 padding
 *   12dp × 2 = 24dp → 内容合计 **236.5dp**；
 * - 状态栏 inset 24dp（典型）时可用高 = 272 − 24 − 24 = **224dp** ≥ 212.5dp（预览 184.5 +
 *   间距 8 + 提示 20）→ 预览仍按宽度铺满 16:9，**底部余量仅约 11.5dp**（header 贴顶，松紧刚好）；
 * - inset 44dp（大刘海）时可用 204dp → 预览等比缩到 176dp（宽约 313dp），仍 16:9、不裁切。
 *
 * 因此取 272dp：单栏贴顶时余量约 10dp 量级，不再像 300dp 那样上下各空 30dp 左右。
 * 分栏时该常量不参与（左栏吃满整页高度，「预览 + 提示行」整体垂直居中）。
 */
private val TOP_CONTENT_HEIGHT = 272.dp

/** 页面（预览、提示、单位 chips、设置项）统一的左右留白 */
private val PAGE_HORIZONTAL_PADDING = 16.dp

/** 预览图上尺寸标注的统一颜色（压暗后的画面上依然可读） */
private val AnnotationColor = Color.White.copy(alpha = 0.85f)

/**
 * 从 DataStore 偏好快照里读出当前字幕样式，读不到的项回落 [SettingConstants] 里的默认值。
 *
 * 本页预览与「播放设置」里「字幕样式」入口的摘要共用本函数，保证两处读的是同一份数据。
 */
internal fun readSubtitleStyleSpec(preferences: Preferences): SubtitleStyleSpec {
    return SubtitleStyleSpec(
        fontSizeUnitMode = preferences[SettingPreferences.PlayerSubtitleFontSizeUnitMode]
            ?: SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_DEFAULT,
        bottomMarginUnitMode = preferences[SettingPreferences.PlayerSubtitleBottomMarginUnitMode]
            ?: SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_DEFAULT,
        fontSizeSp = preferences[SettingPreferences.PlayerSubtitleFontSize]
            ?: SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_DEFAULT,
        bottomMarginDp = preferences[SettingPreferences.PlayerSubtitleBottomMargin]
            ?: SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_DEFAULT,
        fontSizePercent = preferences[SettingPreferences.PlayerSubtitleFontSizePercent]
            ?: SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_DEFAULT,
        bottomMarginPercent = preferences[SettingPreferences.PlayerSubtitleBottomMarginPercent]
            ?: SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_DEFAULT,
    )
}

@Composable
private fun SubtitleStyleSettingContent() {
    val windowInsets = localContentInsets()
    val dataStore = remember { appDataStore }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    // 偏好流只创建一次：单栏/双栏切换时 LazyColumn 会换位置，重复创建会出现两个写回者
    val preferenceFlow = rememberPreferenceFlow(dataStore)
    val chainScrollableLayoutState = rememberChainScrollableLayoutState(
        maxScrollPosition = TOP_CONTENT_HEIGHT,
    )

    // 已持久化的字幕样式：也是实时值的同步来源
    val spec by remember(dataStore) {
        dataStore.data.map { readSubtitleStyleSpec(it) }
    }.collectAsState(initial = SubtitleStyleSpec.Default)

    // 预览用的实时值：拖动滑杆时逐帧更新（此时偏好还没写回），
    // 持久化值变化（进入页面、恢复默认、切换单位）时再同步回来，避免两处不一致
    var liveSpec by remember { mutableStateOf(SubtitleStyleSpec.Default) }
    LaunchedEffect(spec) {
        liveSpec = spec
    }

    val isPercentFontSize =
        liveSpec.fontSizeUnitMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT
    val isPercentBottomMargin =
        liveSpec.bottomMarginUnitMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT

    // 切换单位：先改实时值（滑杆与提示立刻切换），再落盘，不必等 DataStore 回显
    val onFontSizeUnitChange: (Int) -> Unit = { mode ->
        liveSpec = liveSpec.copy(fontSizeUnitMode = mode)
        scope.launch {
            SettingPreferences.editPreferences {
                it[SettingPreferences.PlayerSubtitleFontSizeUnitMode] = mode
            }
        }
    }
    val onBottomMarginUnitChange: (Int) -> Unit = { mode ->
        liveSpec = liveSpec.copy(bottomMarginUnitMode = mode)
        scope.launch {
            SettingPreferences.editPreferences {
                it[SettingPreferences.PlayerSubtitleBottomMarginUnitMode] = mode
            }
        }
    }

    DoubleColumnAutofitLayout(
        modifier = Modifier.fillMaxSize(),
        innerPadding = windowInsets.addPaddingValues(
            addBottom = 0.dp,
        ),
        chainScrollableLayoutState = chainScrollableLayoutState,
        twoColumnMinWidth = 600.dp,
        topContentHeight = TOP_CONTENT_HEIGHT,
        leftContent = { orientation, innerPadding ->
            SubtitlePreviewHeader(
                spec = liveSpec,
                innerPadding = innerPadding,
                // 分栏（宽窗）时左栏高度是整页高，需要整体垂直居中；单栏时贴顶才紧凑
                centerVertically = orientation == Orientation.Horizontal,
            )
        },
    ) { _, innerPadding ->
        ProvidePreferenceLocals(
            flow = preferenceFlow,
        ) {
            LazyColumn(
                contentPadding = innerPadding,
                state = listState,
            ) {
                preferenceCategory(
                    key = "size",
                    title = { Text("字号") }
                )
                if (isPercentFontSize) {
                    // 百分比单位：字号 = 画面**短边** min(宽, 高) × 比例（步长 0.5%）
                    item(key = "font_size_percent") {
                        FloatSliderItem(
                            preferenceKey = SettingPreferences.PlayerSubtitleFontSizePercent.name,
                            defaultValue = SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_DEFAULT,
                            valueRange = SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MIN..
                                    SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_MAX,
                            valueSteps = 27,
                            liveValue = liveSpec.fontSizePercent,
                            onLiveValueChange = {
                                liveSpec = liveSpec.copy(fontSizePercent = it)
                            },
                            title = { Text("字号（占画面短边）") },
                            valueText = { Text("%.1f%%".format(it * 100)) },
                        )
                    }
                } else {
                    // 固定值单位：10..40 共 31 档，每档 1sp（valueSteps = 值个数 - 2）
                    item(key = "font_size_fixed") {
                        IntSliderItem(
                            preferenceKey = SettingPreferences.PlayerSubtitleFontSize.name,
                            defaultValue = SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_DEFAULT,
                            valueRange = SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MIN..
                                    SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MAX,
                            valueSteps = SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MAX -
                                    SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_MIN - 1,
                            liveValue = liveSpec.fontSizeSp,
                            onLiveValueChange = {
                                liveSpec = liveSpec.copy(fontSizeSp = it)
                            },
                            title = { Text("字号") },
                            valueText = { Text("${it}sp") },
                        )
                    }
                }
                // 字号单位：紧跟在自己的滑杆下面（字号百分比按画面短边）
                item(key = "font_size_unit") {
                    UnitChipsRow(
                        fixedLabel = "固定值（sp）",
                        percentLabel = "画面短边百分比",
                        currentMode = liveSpec.fontSizeUnitMode,
                        onSelect = onFontSizeUnitChange,
                    )
                }

                preferenceCategory(
                    key = "position",
                    title = { Text("位置") }
                )
                if (isPercentBottomMargin) {
                    // 百分比单位：距底距离 = 画面高度 × 比例（0% ~ 25%，步长 0.5%）
                    item(key = "bottom_margin_percent") {
                        FloatSliderItem(
                            preferenceKey = SettingPreferences.PlayerSubtitleBottomMarginPercent.name,
                            defaultValue = SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_DEFAULT,
                            valueRange = SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MIN..
                                    SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_MAX,
                            valueSteps = 49,
                            liveValue = liveSpec.bottomMarginPercent,
                            onLiveValueChange = {
                                liveSpec = liveSpec.copy(bottomMarginPercent = it)
                            },
                            title = { Text("距底部距离（占画面高度）") },
                            valueText = { Text("%.1f%%".format(it * 100)) },
                        )
                    }
                } else {
                    // 固定值单位：0..200dp，每档 1dp
                    item(key = "bottom_margin_fixed") {
                        IntSliderItem(
                            preferenceKey = SettingPreferences.PlayerSubtitleBottomMargin.name,
                            defaultValue = SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_DEFAULT,
                            valueRange = SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MIN..
                                    SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MAX,
                            valueSteps = SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MAX -
                                    SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_MIN - 1,
                            liveValue = liveSpec.bottomMarginDp,
                            onLiveValueChange = {
                                liveSpec = liveSpec.copy(bottomMarginDp = it)
                            },
                            title = { Text("距底部距离") },
                            summary = {
                                Text("控制器隐藏时字幕距离画面底部的距离；控制器显示时字幕自动避让到底栏上方")
                            },
                            valueText = { Text("${it}dp") },
                        )
                    }
                }
                // 位置单位：紧跟在自己的滑杆下面（距底百分比按画面高度）
                item(key = "bottom_margin_unit") {
                    UnitChipsRow(
                        fixedLabel = "固定值（dp）",
                        percentLabel = "画面高度百分比",
                        currentMode = liveSpec.bottomMarginUnitMode,
                        onSelect = onBottomMarginUnitChange,
                    )
                }

                preferenceCategory(
                    key = "other",
                    title = { Text("其它") }
                )
                preference(
                    key = "reset_subtitle_style",
                    title = { Text("恢复默认") },
                    summary = { Text("字号 16sp、距底部 12dp，两处都用固定值单位") },
                    onClick = {
                        // 先改实时值，预览与滑杆立刻回到默认，不必等 DataStore 回显
                        liveSpec = SubtitleStyleSpec.Default
                        scope.launch {
                            SettingPreferences.editPreferences {
                                it[SettingPreferences.PlayerSubtitleFontSizeUnitMode] =
                                    SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_DEFAULT
                                it[SettingPreferences.PlayerSubtitleBottomMarginUnitMode] =
                                    SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_DEFAULT
                                it[SettingPreferences.PlayerSubtitleFontSize] =
                                    SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_DEFAULT
                                it[SettingPreferences.PlayerSubtitleBottomMargin] =
                                    SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_DEFAULT
                                it[SettingPreferences.PlayerSubtitleFontSizePercent] =
                                    SettingConstants.PLAYER_SUBTITLE_FONT_SIZE_PERCENT_DEFAULT
                                it[SettingPreferences.PlayerSubtitleBottomMarginPercent] =
                                    SettingConstants.PLAYER_SUBTITLE_BOTTOM_MARGIN_PERCENT_DEFAULT
                            }
                        }
                    },
                )
            }
        }
    }
}

/**
 * 预览画面 + 紧贴其下方的提示行（同一行内：左「距底部」、右「字号」）。
 *
 * 单栏时它是顶部可折叠 header（高度固定为 [TOP_CONTENT_HEIGHT]），双栏时是左侧一栏
 * （吃满整屏高度）。
 *
 * 高度分配采用「下方提示行优先、预览只吃剩余」的测量顺序：
 * - 提示行是不带 weight 的普通子项 → Column 先按内容测量它的真实高度（系统字体放大、换行
 *   都不会被裁，恒可见）；
 * - 预览区用 `weight(1f, fill = false)` → 最多只能用到扣掉提示行与间距之后的高度，够用时
 *   不强行撑满（不 fill），因此预览与提示行之间始终只有固定的 8dp，不会被拉开；
 * - 剩余高度按 [centerVertically] 处理：单栏（[centerVertically] = false）**贴顶**排布，
 *   header 高度已按内容收紧（见 [TOP_CONTENT_HEIGHT]），多余空间只留在底部且很小；
 *   分栏（[centerVertically] = true）时左栏吃满整页高度，`Arrangement.Center` 把
 *   「预览 + 提示行」整体垂直居中。
 * 预览始终 16:9：高度由「可用宽 × 9/16」与「剩余高度」取小，宽度再由高度反推，
 * 高度受限时只会整体变小，不裁切也不拉伸；极矮窗口下可以压到 0 高，提示行仍然可见。
 *
 * 提示行的数值都来自实时值（[spec]），随滑杆拖动即时刷新，且**只显示当前单位**
 * （`16sp` 或 `4.5%`）；单位选择在设置项列表里各自滑杆的正下方，不在本 header 内。
 *
 * @param spec 当前字幕样式（实时值）
 * @param innerPadding 页面内容的内边距（含状态栏等系统栏留白）
 * @param centerVertically 是否把整组内容垂直居中：分栏（宽窗）时传 true（左栏高 = 整页高，
 * 居中更均衡）；单栏时传 false（贴顶才紧凑，余量留在底部）
 */
@Composable
private fun SubtitlePreviewHeader(
    spec: SubtitleStyleSpec,
    innerPadding: PaddingValues,
    centerVertically: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = PAGE_HORIZONTAL_PADDING, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // 分栏时整体垂直居中，多余空白均分到上下；单栏时贴顶，余量只落在底部（更紧凑）
        verticalArrangement = if (centerVertically) Arrangement.Center else Arrangement.Top,
    ) {
        // weight(1f, fill = false)：Column 先测量下方提示行的真实高度，预览最多只用剩下的高度；
        // fill = false 让预览够用时不被拉伸，剩余空间按上面的 verticalArrangement 处理，
        // 因此单栏贴顶、分栏居中都不会把提示行推离预览底边
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            // 高度取「按宽度算的 16:9 理想高度」与「剩余高度」的较小值，再反推宽度
            val previewHeight = minOf(maxWidth * 9f / 16f, maxHeight).coerceAtLeast(0.dp)
            val previewWidth = previewHeight * 16f / 9f
            SubtitleStylePreviewCard(
                spec = spec,
                modifier = Modifier
                    .width(previewWidth)
                    .height(previewHeight),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // 提示紧贴预览下方、左右各一行：左侧「距底部」、右侧「字号」，只显示当前单位的数值
        // （固定值 sp/dp 或百分比），数值来自实时值，随滑杆拖动即时刷新。
        // 两侧各占一半可用宽（weight），窄屏放不下时各自单行省略，不会被对方挤成空。
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SliderHintText(
                text = "距底部 ${spec.bottomMarginDescription()}",
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f, fill = false),
            )
            SliderHintText(
                text = "字号 ${spec.fontSizeDescription()}",
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}

/**
 * 预览下方的一行纯文字提示（小号灰字，只显示当前单位的数值）。
 *
 * 提示用于「距底在左、字号在右」的同一行（由 [SubtitlePreviewHeader] 用
 * `Arrangement.SpaceBetween` 排布）：左侧一段 [textAlign] 取 [TextAlign.Start]，
 * 右侧一段取 [TextAlign.End]。行的左右边界由父级 [SubtitlePreviewHeader] 的
 * `padding(horizontal = ...)` 提供，这里只补 2dp 上间距，让提示紧贴预览下方又不显拥挤；
 * 窄屏放不下时单行省略（[TextOverflow.Ellipsis]），不会换行把布局撑高。
 *
 * @param text 提示文本，例如 `"字号 16sp"` / `"距底部 3.5%"`
 * @param textAlign 文本在自身宽度内的对齐方式（左段 [TextAlign.Start]、右段 [TextAlign.End]）
 * @param modifier 布局修饰符（调用方负责宽度与权重）
 */
@Composable
private fun SliderHintText(
    text: String,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.padding(top = 2.dp),
    )
}

/**
 * 单位模式选择：两个 [FilterChip]，放在**各自滑杆的正下方**（不在预览下方）。
 *
 * 选中后回调 [onSelect]，由调用方立即更新实时值（滑杆、预览与提示马上切换）并写回偏好。
 *
 * @param fixedLabel 固定值选项的文案（字号填 sp、位置填 dp）
 * @param percentLabel 百分比选项的文案（字号按画面短边、位置按画面高度，基准不同不能混用）
 * @param currentMode 当前单位模式（取值见 [SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_FIXED] /
 * [SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT]）
 * @param onSelect 选中回调
 */
@Composable
private fun UnitChipsRow(
    fixedLabel: String,
    percentLabel: String,
    currentMode: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = PAGE_HORIZONTAL_PADDING, end = PAGE_HORIZONTAL_PADDING, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = currentMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_FIXED,
            onClick = { onSelect(SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_FIXED) },
            label = { Text(fixedLabel) },
        )
        FilterChip(
            selected = currentMode == SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT,
            onClick = { onSelect(SettingConstants.PLAYER_SUBTITLE_UNIT_MODE_PERCENT) },
            label = { Text(percentLabel) },
        )
    }
}

/**
 * 整数单位的样式滑杆条目（字号 sp / 距底部距离 dp）。
 *
 * 拖动过程中只更新实时值（[onLiveValueChange] → 预览逐帧跟随），松手时才写回持久化偏好：
 * 这一分工由 [SliderPreference] 的 `value` / `sliderValue` 两个参数实现——库内部只在
 * `onValueChangeFinished` 里回调 `onValueChange`。
 *
 * 注意 [rememberPreferenceState] 的泛型必须与 DataStore 里存的类型一致（这里是 Int），
 * 否则读取时会类型转换失败。
 *
 * @param preferenceKey 偏好键名
 * @param defaultValue 读不到时的默认值
 * @param valueRange 取值区间
 * @param valueSteps 中间刻度数（区间数 − 1）
 * @param liveValue 当前实时值（预览用）
 * @param onLiveValueChange 实时值变化回调（拖动中逐帧触发，不落盘）
 * @param title 标题
 * @param summary 描述
 * @param valueText 数值文本
 */
@Composable
private fun IntSliderItem(
    preferenceKey: String,
    defaultValue: Int,
    valueRange: ClosedRange<Int>,
    valueSteps: Int,
    liveValue: Int,
    onLiveValueChange: (Int) -> Unit,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    valueText: @Composable (Int) -> Unit,
) {
    // 已持久化的值：松手（onValueChange）时才写入
    val committed = rememberPreferenceState(preferenceKey, defaultValue)
    SliderPreference(
        value = committed.value.toFloat(),
        onValueChange = { committed.value = it.roundToInt() },
        sliderValue = liveValue.toFloat(),
        onSliderValueChange = { onLiveValueChange(it.roundToInt()) },
        title = title,
        modifier = Modifier.fillMaxWidth(),
        valueRange = valueRange.start.toFloat()..valueRange.endInclusive.toFloat(),
        valueSteps = valueSteps,
        summary = summary,
        valueText = { valueText(it.roundToInt()) },
    )
}

/**
 * 比例单位的样式滑杆条目（百分比模式下的字号与距底部距离）。
 *
 * 与 [IntSliderItem] 同一套逻辑，只是 [rememberPreferenceState] 的泛型是 Float
 * （对应 DataStore 里的 float 偏好）。
 *
 * @param preferenceKey 偏好键名
 * @param defaultValue 读不到时的默认值
 * @param valueRange 取值区间
 * @param valueSteps 中间刻度数（区间数 − 1）
 * @param liveValue 当前实时值（预览用）
 * @param onLiveValueChange 实时值变化回调（拖动中逐帧触发，不落盘）
 * @param title 标题
 * @param summary 描述
 * @param valueText 数值文本
 */
@Composable
private fun FloatSliderItem(
    preferenceKey: String,
    defaultValue: Float,
    valueRange: ClosedRange<Float>,
    valueSteps: Int,
    liveValue: Float,
    onLiveValueChange: (Float) -> Unit,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    valueText: @Composable (Float) -> Unit,
) {
    val committed = rememberPreferenceState(preferenceKey, defaultValue)
    SliderPreference(
        value = committed.value,
        onValueChange = { committed.value = it },
        sliderValue = liveValue,
        onSliderValueChange = onLiveValueChange,
        title = title,
        modifier = Modifier.fillMaxWidth(),
        valueRange = valueRange,
        valueSteps = valueSteps,
        summary = summary,
        valueText = valueText,
    )
}

/**
 * 16:9 字幕样式预览卡片。
 *
 * 画面按比例渲染（不使用 [SubtitleStyleSpec.resolve]）：字号与距底距离都先折算成比例，
 * 再乘以预览框的对应边长，因此无论手机、平板还是桌面窗口，预览与播放器的观感都一致。
 * 两侧基准**不同**：
 * - 字号按**画面短边** `min(宽, 高)` 缩放（预览是 16:9，短边即高；但按短边写语义才与播放器
 *   百分比模式的「占画面短边」一致，横屏竖屏都不会忽大忽小）；
 * - 距底距离仍按**画面高度**缩放（字幕停靠在底部，参照高度更符合直觉）。
 * 固定值单位按 [SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT] 这个参考边长折算成比例。
 *
 * 卡片尺寸由调用方给定（宽高已按 16:9 算好）。具体的字号/距底数值不画在画面上，
 * 而是由调用方在卡片下方用一行提示（左「距底部」、右「字号」）与各自滑杆下的单位选择展示；
 * 画面里只保留预览比例标注与距底标尺。
 *
 * @param spec 当前字幕样式（实时值）
 * @param modifier 布局修饰符（已包含宽高）
 */
@Composable
private fun SubtitleStylePreviewCard(
    spec: SubtitleStyleSpec,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    // 比例换算：字号看短边、距底看高度，与播放器百分比模式同一套规则
    val fontFraction = spec.fontSizeFraction()
    val marginFraction = spec.bottomMarginFraction()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black),
    ) {
        // 背景画面：用首页横幅图当「视频画面」示意（16:9 横幅，接近真实视频比例）
        Image(
            painter = painterResource(Res.drawable.top_bg1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        // 压暗画面：让白色字幕的对比度接近真实播放场景
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.25f)),
        )

        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val frameWidth = maxWidth
            val frameHeight = maxHeight
            // 字号基准是画面短边：16:9 下短边就是高度，但语义按 min(宽, 高) 写
            val frameShortSide = minOf(frameWidth, frameHeight)
            // 预览字号：画面短边 × 字号比例，再按密度换算成 sp（与播放器百分比模式同一算法）
            val previewFontSize = with(density) { (frameShortSide * fontFraction).toSp() }
            val previewBottomMargin = frameHeight * marginFraction
            // 标尺起点：字幕底边（距画面底部 marginFraction 处）
            val rulerTop = frameHeight - previewBottomMargin

            // 示例字幕：与播放器共用 SubtitleTextBox，字号/位置完全由比例决定；
            // 左右固定 20dp 边距与播放器字幕槽位一致（[SUBTITLE_FRAME_HORIZONTAL_PADDING]），
            // 因此预览里的换行宽度就是实际播放时的换行宽度（所见即所得）
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(
                        start = SUBTITLE_FRAME_HORIZONTAL_PADDING,
                        end = SUBTITLE_FRAME_HORIZONTAL_PADDING,
                        bottom = previewBottomMargin,
                    ),
                contentAlignment = Alignment.BottomCenter,
            ) {
                SubtitleTextBox(
                    text = PREVIEW_SUBTITLE_TEXT,
                    fontSize = previewFontSize,
                )
            }

            // 距底部距离标尺：竖直虚线 + 两端短横线，标出「字幕底边」与「画面底边」
            Canvas(modifier = Modifier.matchParentSize()) {
                val x = 14.dp.toPx()
                val topY = rulerTop.toPx()
                val bottomY = size.height
                val tick = 4.dp.toPx()
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = AnnotationColor,
                    start = Offset(x, topY),
                    end = Offset(x, bottomY),
                    strokeWidth = strokeWidth,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
                )
                drawLine(
                    color = AnnotationColor,
                    start = Offset(x - tick, topY),
                    end = Offset(x + tick, topY),
                    strokeWidth = strokeWidth,
                )
                drawLine(
                    color = AnnotationColor,
                    start = Offset(x - tick, bottomY),
                    end = Offset(x + tick, bottomY),
                    strokeWidth = strokeWidth,
                )
            }

            // 左上角：预览比例与参考短边（具体的字号/距底数值改到卡片下方展示）
            AnnotationLabel(
                text = "预览 16:9 · 参考短边 ${SUBTITLE_PREVIEW_REFERENCE_FRAME_HEIGHT.value.toInt()}dp",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 8.dp),
            )
        }
    }
}

/**
 * 预览图上的尺寸标注标签：小号白字 + 半透明黑底圆角，压在画面上仍清晰可读。
 *
 * @param text 标注文本
 * @param modifier 布局修饰符（调用方负责对齐与外边距）
 */
@Composable
private fun AnnotationLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = AnnotationColor,
        fontSize = 10.sp,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0x99000000))
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}
