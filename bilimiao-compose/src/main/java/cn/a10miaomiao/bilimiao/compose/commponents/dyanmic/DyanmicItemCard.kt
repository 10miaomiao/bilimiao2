package cn.a10miaomiao.bilimiao.compose.commponents.dyanmic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
@Composable
fun DynamicItemCard(
    modifier: Modifier = Modifier,
    item: bilibili.app.dynamic.v2.DynamicItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(
                color = MaterialTheme.colorScheme.surface,
            )
            .clickable(onClick = onClick),
    ) {
        for(module in item.modules) {
            DynamicModuleBox(module = module)
        }
    }
}
