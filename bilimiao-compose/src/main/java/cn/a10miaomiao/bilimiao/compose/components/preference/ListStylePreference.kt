package cn.a10miaomiao.bilimiao.compose.components.preference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.rememberPreferenceState
import java.lang.Float.min

inline fun LazyListScope.listStylePreference(
    key: String,
    defaultValue: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    crossinline rememberState: @Composable () -> MutableState<Int> = {
        rememberPreferenceState(key, defaultValue)
    },
) {
    item(key = key, contentType = "listStylePreference") {
        val state = rememberState()
        ListStylePreference(
            state = state,
            modifier = modifier,
        )
    }
}

@Composable
fun ListStylePreference(
    state: MutableState<Int>,
    modifier: Modifier = Modifier,
) {
    var value by state
    ListStylePreference(
        value = value,
        onValueChange = { value = it },
        modifier,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ListStylePreference(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialValue = remember { value }
    Preference(
        modifier = modifier,
        title = {
            Text("列表样式")
        },
        summary = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        .takeIf { value == 0 },
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    onClick = {
                        onValueChange(0)
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                        ) {
                            ListStyle1(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .align(Alignment.Center)
                                    .padding(top = 8.dp)
                                    .height(50.dp),
                                maxWidth = maxWidth,
                            )
                        }
                        Text(
                            text = "列表式",
                            color = if (value == 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        .takeIf { value == 1 },
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    onClick = {
                        onValueChange(1)
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                        ) {
                            ListStyle2(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .align(Alignment.Center)
                                    .padding(top = 8.dp)
                                    .height(50.dp),
                                maxWidth = maxWidth,
                            )
                        }
                        Text(
                            text = "卡片式",
                            color = if (value == 1) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun ListStyle1(
    maxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val scale = min(maxWidth / 400.dp, 0.5f)
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = modifier,
    ) {
        scale(
            scaleX = scale,
            scaleY = scale,
            pivot = Offset(0f, 0f)
        ) {
            val radius = 8.dp.toPx()
            this.drawRoundRect(
                color = color,
                topLeft = Offset(0f, 0f),
                cornerRadius = CornerRadius(radius),
                size = Size(142.dp.toPx(), height = 100.dp.toPx())
            )
            this.drawRoundRect(
                color = color,
                topLeft = Offset(150.dp.toPx(), 0f),
                cornerRadius = CornerRadius(radius),
                size = Size(250.dp.toPx(), height = 20.dp.toPx())
            )
            this.drawRoundRect(
                color = color,
                topLeft = Offset(150.dp.toPx(), 24.dp.toPx()),
                cornerRadius = CornerRadius(radius),
                size = Size(120.dp.toPx(), height = 20.dp.toPx())
            )
        }

    }
}

@Composable
private fun ListStyle2(
    maxWidth: Dp,
    modifier: Modifier = Modifier,
) {
    val scale = min(maxWidth / 400.dp, 0.4f)
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(
        modifier = modifier,
    ) {
        scale(
            scaleX = scale,
            scaleY = scale,
            pivot = Offset(0f, 0f)
        ) {
            val radius = 8.dp.toPx()
            this.drawRoundRect(
                color = color,
                topLeft = Offset(0f, 0f),
                cornerRadius = CornerRadius(radius),
                size = Size(192.dp.toPx(), height = 120.dp.toPx())
            )
            this.drawRoundRect(
                color = color,
                topLeft = Offset(208.dp.toPx(), 0f),
                cornerRadius = CornerRadius(radius),
                size = Size(192.dp.toPx(), height = 120.dp.toPx())
            )

            this.drawRoundRect(
                color = color,
                topLeft = Offset(0f, 128.dp.toPx()),
                cornerRadius = CornerRadius(radius),
                size = Size(192.dp.toPx(), height = 20.dp.toPx())
            )
            this.drawRoundRect(
                color = color,
                topLeft = Offset(208.dp.toPx(), 128.dp.toPx()),
                cornerRadius = CornerRadius(radius),
                size = Size(192.dp.toPx(), height = 20.dp.toPx())
            )
        }

    }
}