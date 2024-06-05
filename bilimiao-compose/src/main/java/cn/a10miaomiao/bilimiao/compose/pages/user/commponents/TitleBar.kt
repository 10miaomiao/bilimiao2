package cn.a10miaomiao.bilimiao.compose.pages.user.commponents

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menufold
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Menuunfold

@Composable
fun TitleBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    action: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
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