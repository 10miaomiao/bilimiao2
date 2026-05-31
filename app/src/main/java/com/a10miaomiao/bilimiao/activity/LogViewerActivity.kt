package com.a10miaomiao.bilimiao.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config

class LogViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val logSummary = intent.getStringExtra("log_summary") ?: ""
        setContent {
            LogViewerScreen(
                logSummary = logSummary,
                onBack = { onBackPressedDispatcher.onBackPressed() },
                onCopy = { copyLogText(it) }
            )
        }
    }

    private fun copyLogText(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("log", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "复制成功(●'◡'●)", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun LogViewerScreen(
    logSummary: String,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
) {
    val context = LocalContext.current
    val bgColor = remember { context.config.windowBackgroundColor }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.log_viewer)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            },
            actions = {
                IconButton(onClick = { onCopy(logSummary) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                }
            }
        )
        SelectionContainer(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color(bgColor))
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = logSummary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                fontSize = 14.sp,
            )
        }
    }
}
