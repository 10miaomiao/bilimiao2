package cn.a10miaomiao.bilimiao.compose.common.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class AppInfoDesktop : AppInfo {

    override val versionName: String = "2.5.0-desktop"
    override val versionCode: Long = 100L

    @Composable
    override fun AppIcon(modifier: Modifier) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text("B")
        }
    }
}
