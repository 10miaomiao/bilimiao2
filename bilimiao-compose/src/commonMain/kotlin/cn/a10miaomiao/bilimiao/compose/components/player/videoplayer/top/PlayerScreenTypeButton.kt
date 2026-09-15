@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cn.a10miaomiao.bilimiao.compose.components.player.videoplayer.top

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.openani.mediamp.features.AspectRatioMode

/**
 * 画面比例（对齐旧版 bilimiao 播放器「更多」菜单中的「画面比例」）
 *
 * [value] 沿用旧版 GSYVideoType 的取值，与
 * [com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences.PlayerScreenType]
 * 偏好项所保存的历史值保持一致。
 *
 * @param value 用于持久化的取值
 * @param label 菜单显示文本
 */
enum class PlayerScreenType(val value: Int, val label: String) {
    /** 默认比例：保持视频原始比例适应画面框 */
    Default(0, "默认比例"),

    /** 16:9：画面框固定为 16:9 并居中，画面拉伸填满 */
    Ratio16x9(1, "16:9"),

    /** 4:3：画面框固定为 4:3 并居中，画面拉伸填满 */
    Ratio4x3(2, "4:3"),

    /** 全屏裁减：保持原始比例填满画面框，超出部分裁掉 */
    Crop(4, "全屏裁减"),

    /** 全屏拉伸：拉伸填满画面框，画面可能变形 */
    Stretch(-4, "全屏拉伸"),
    ;

    /**
     * 交给播放器的缩放模式。
     *
     * 16:9 / 4:3 的画面框尺寸已由布局限制为目标比例，因此让画面拉伸填满该框。
     */
    val aspectRatioMode: AspectRatioMode
        get() = when (this) {
            Default -> AspectRatioMode.FIT
            Crop -> AspectRatioMode.CROP
            Ratio16x9, Ratio4x3, Stretch -> AspectRatioMode.STRETCH
        }

    /** 画面框宽高比；为 `null` 时画面填满整个播放器画面区域 */
    val frameAspectRatio: Float?
        get() = when (this) {
            Ratio16x9 -> 16f / 9f
            Ratio4x3 -> 4f / 3f
            else -> null
        }

    companion object {
        fun ofValue(value: Int): PlayerScreenType =
            entries.firstOrNull { it.value == value } ?: Default
    }
}

/**
 * 播放器顶栏「画面比例」按钮
 *
 * 点击弹出比例菜单，当前比例带选中标记。
 *
 * @param screenType 当前画面比例
 * @param onValueChange 比例变更回调
 * @param modifier 布局修饰符
 */
@Composable
fun PlayerScreenTypeButton(
    screenType: PlayerScreenType,
    onValueChange: (PlayerScreenType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Rounded.AspectRatio, "画面比例")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            PlayerScreenType.entries.forEach { type ->
                val selected = type == screenType
                DropdownMenuItem(
                    text = {
                        val color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            LocalContentColor.current
                        }
                        CompositionLocalProvider(LocalContentColor provides color) {
                            Text(type.label)
                        }
                    },
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                Icons.Rounded.Check,
                                "已选择",
                                Modifier.size(18.dp),
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onValueChange(type)
                    },
                )
            }
        }
    }
}
