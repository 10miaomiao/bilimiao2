package cn.a10miaomiao.bilimiao.compose.pages.setting.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun ProxyServerCard(
    name: String,
    host: String,
    isTrust: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 5.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = name,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 5.dp),
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = host,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.outline,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isTrust) {
                        Text(
                            text = "已信任",
                            color = Color.Red,
                        )
                    } else {
                        Text(
                            text = "未信任",
                            color = Color.Gray,
                        )
                    }
                }
            }
        }
    }
}