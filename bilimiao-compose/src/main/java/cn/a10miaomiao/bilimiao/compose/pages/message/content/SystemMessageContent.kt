package cn.a10miaomiao.bilimiao.compose.pages.message.content

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

internal class SystemMessageContentModel(
    override val di: DI,
) : ViewModel(), DIAware


@Composable
fun SystemMessageContent() {
    val viewModel: SystemMessageContentModel = diViewModel()
    val windowInsets = localContentInsets()

    Column {
        Text(text = "回复我的")
    }
}