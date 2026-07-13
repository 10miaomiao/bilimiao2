package cn.a10miaomiao.bilimiao.compose.common.webview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
@Composable
actual fun WebViewContainer(
    url: String,
    modifier: Modifier,
    config: WebViewConfig,
    callbacks: WebViewCallbacks,
    onHandleReady: (WebViewHandle) -> Unit,
) {
    val stubHandle = remember {
        object : WebViewHandle {
            override fun loadUrl(url: String) {}
            override fun reload() {}
            override fun evaluateJavascript(script: String, callback: ((String?) -> Unit)?) { callback?.invoke(null) }
            override fun addJavascriptInterface(name: String, handler: (String) -> Unit) {}
            override fun removeJavascriptInterface(name: String) {}
            override fun stopLoading() {}
            override fun canGoBack(): Boolean = false
            override fun goBack() {}
        }
    }

    DisposableEffect(Unit) {
        onHandleReady(stubHandle)
        onDispose {}
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(
                text = "暂不支持APP内WebView",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "即将用外部浏览器打开：\n$url",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(32.dp))
            val platformContext = LocalPlatformContext.current
            Button(onClick = { platformContext.openUrl(url) }) {
                Text("用浏览器打开")
            }
        }
    }
}
