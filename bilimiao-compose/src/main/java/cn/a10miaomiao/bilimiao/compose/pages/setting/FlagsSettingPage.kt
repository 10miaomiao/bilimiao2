package cn.a10miaomiao.bilimiao.compose.pages.setting

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import kotlinx.serialization.Serializable

@Serializable
class FlagsSettingPage : ComposePage() {

    @Composable
    override fun Content() {
        FlagsSettingPageContent()
    }
}

@Composable
private fun FlagsSettingPageContent() {
    PageConfig(
        title = "实验性功能"
    )
    val windowInsets = localContentInsets()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = windowInsets.leftDp.dp,
                end = windowInsets.rightDp.dp,
            )
    ) {
        item("top") {
            Spacer(
                modifier = Modifier.height(windowInsets.topDp.dp)
            )
        }
        item("empty") {
            Text(
                text = "当前没有可用的实验性功能。",
                modifier = Modifier.padding(16.dp),
            )
        }
        item("bottom") {
            Spacer(
                modifier = Modifier.height(
                    windowInsets.bottom
                )
            )
        }
    }
}
