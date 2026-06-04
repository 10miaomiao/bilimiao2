package cn.a10miaomiao.bilimiao.compose.components.status

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import java.net.UnknownHostException

@Composable
fun BiliFailBox(
    e: Any,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    val message = remember(e) {
        when (e) {
            is String -> e
            is UnknownHostException -> "无法连接到御坂网络"
//            is JsonSyntaxException -> "数据解析失败: $e"
            is Exception -> e.message ?: e.toString()
            else -> e.toString()
        }
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .widthIn(max = 150.dp)
                .aspectRatio(1f),
            painter = painterResource(id = R.drawable.bili_fail_img),
            contentDescription = "fail",
        )
        Text(
            modifier = Modifier.padding(16.dp),
            text = message,
            color = textColor,
        )
    }
}