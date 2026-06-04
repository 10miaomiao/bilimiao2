package cn.a10miaomiao.bilimiao.compose.components.miao

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min

private val TitleBarHeight = 48.dp

@Composable
fun MiaoTitleBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    icon: @Composable () -> Unit = {},
    action: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(TitleBarHeight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.widthIn(min = 16.dp),
                ) {
                    icon()
                }
                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    title()
                }
                Row(
                    modifier = Modifier.padding(
                        horizontal = 4.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    action()
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
            )
        }
    }
}