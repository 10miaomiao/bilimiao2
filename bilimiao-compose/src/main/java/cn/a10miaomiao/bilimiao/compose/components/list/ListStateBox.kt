package cn.a10miaomiao.bilimiao.compose.components.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ListStateBox(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    finished: Boolean = false,
    fail: String? = null,
    listData: List<*>? = null,
    loadMore: () -> Unit = {},
) {
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (finished) {
                Text(
                    "下面没有了",
                    modifier = Modifier.padding(start = 5.dp),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 14.sp,
                )
            } else if (fail?.isNotBlank() == true) {
                TextButton(onClick = loadMore) {
                    Text(
                        fail,
                        modifier = Modifier.padding(start = 5.dp),
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp,
                    )
                }
            } else if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                )
                Text(
                    "加载中",
                    modifier = Modifier.padding(start = 5.dp),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 14.sp,
                )
            } else if (listData?.size == 0){
                Text(
                    "空空如也",
                    modifier = Modifier.padding(start = 5.dp),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 14.sp,
                )
            } else {
                TextButton(onClick = loadMore) {
                    Text(
                        "加载更多",
                        modifier = Modifier.padding(start = 5.dp),
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        if (!loading && !finished && fail != null && listData?.size != 0) {
            loadMore()
        }
    }
}