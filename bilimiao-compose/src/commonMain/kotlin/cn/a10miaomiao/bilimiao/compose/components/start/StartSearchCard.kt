package cn.a10miaomiao.bilimiao.compose.components.start

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard

@Composable
fun StartSearchCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onScannerClick: () -> Unit,
) {
    MiaoOutlinedCard(
        modifier = modifier,
        onClick = {
            onClick()
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp),
                imageVector = Icons.Filled.Search,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = "请输入ID或关键字",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
            )
            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
                    .clickable(onClick = onScannerClick),
                imageVector = Icons.Filled.CameraAlt,
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }
    }
}
