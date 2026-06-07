package cn.a10miaomiao.bilimiao.compose.common.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        Text(
            text = "WebView is not supported on Desktop",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}
