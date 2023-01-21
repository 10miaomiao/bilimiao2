package cn.a10miaomiao.bilimiao.compose.pages.setting.commponents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProxyServerForm(
    name: String,
    onNameChange: (String) -> Unit,
    host: String,
    onHostChange: (String) -> Unit,
    isTrust: Boolean,
    onIsTrustChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            value = name,
            onValueChange = onNameChange,
            label = {
                Text(text = "服务器名称")
            },
            singleLine = true,
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            value = host,
            onValueChange = onHostChange,
            label = {
                Text(text = "服务器地址")
            },
            singleLine = true,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isTrust,
                onCheckedChange = onIsTrustChange,
            )
            Text(
                text = "信任该服务器",
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Text(
            text = """注意事项：
1、服务器名称任意填写，如：猫猫的服务器、鼠鼠的服务器。
2、服务器地址即服务器域名，如：10miaomiao.cn、fuck.bilibili.com。
3、勾选"信任该服务器"则会提交登录信息(token)至该服务器，请确认服务器安全后再勾选。
4、勾选"信任该服务器"后,如发现帐号有异常行为,请立即修改密码，并取消信任或删除该服务器。""",
            modifier = Modifier.padding(vertical = 5.dp),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}