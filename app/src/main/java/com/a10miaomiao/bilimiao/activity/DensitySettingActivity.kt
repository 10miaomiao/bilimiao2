@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.a10miaomiao.bilimiao.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.platform.DensitySettingLauncher
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.ScreenDpiUtil

class DensitySettingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this) {
            handleBack()
        }
        setContent {
            BilimiaoActivityTheme {
                DensitySettingScreen(
                    onBack = { onBackPressedDispatcher.onBackPressed() },
                    onConfirm = { dpi, fontScale -> setCustomConfiguration(dpi, fontScale) }
                )
            }
        }
    }

    /** 返回时若修改过配置，则重启应用使新配置对整个应用生效 */
    private fun handleBack() {
        if (isChanged()) {
            val componentName = packageManager.getLaunchIntentForPackage(packageName)!!.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        } else {
            finish()
        }
    }

    private fun setCustomConfiguration(dpi: Int, fontScale: Float) {
        ScreenDpiUtil.saveCustomConfiguration(dpi, fontScale)
        reStartActivity()
    }

    private fun reStartActivity() {
        startActivity(
            intent.apply {
                putExtra(EXTRA_CHANGED, true)
                addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            }
        )
        finish()
    }

    private fun isChanged(): Boolean {
        return intent.getBooleanExtra(EXTRA_CHANGED, false)
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration: Configuration = newBase.resources.configuration
        ScreenDpiUtil.readCustomConfiguration(configuration)
        val newContext = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(newContext)
    }

    companion object {
        private const val EXTRA_CHANGED = "changed"
    }
}

/** 应用内 DPI 设置页入口的 Android 实现（在 MainActivity 的 DI 中绑定） */
class DensitySettingLauncherAndroid(
    private val context: Context,
) : DensitySettingLauncher {

    override fun openDensitySetting() {
        context.startActivity(Intent(context, DensitySettingActivity::class.java))
    }
}

@Composable
private fun DensitySettingScreen(
    onBack: () -> Unit,
    onConfirm: (Int, Float) -> Unit,
) {
    val context = LocalContext.current
    val defaultDpi = remember { ScreenDpiUtil.getDefaultDpi() }
    val defaultFontScale = remember { ScreenDpiUtil.getDefaultFontScale() }
    val currentDpi = remember { context.resources.configuration.densityDpi }
    val currentFontScale = remember { context.resources.configuration.fontScale }
    var dpiText by remember { mutableStateOf(currentDpi.toString()) }
    var fontScaleText by remember { mutableStateOf(currentFontScale.toString()) }
    val dpi = dpiText.toIntOrNull()?.takeIf { it > 0 }
    val fontScale = fontScaleText.toFloatOrNull()?.takeIf { it > 0f }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.density_setting))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = dpiText,
                onValueChange = { dpiText = it.trim() },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "应用内 DPI")
                },
                supportingText = {
                    Text(
                        text = if (dpi == null) {
                            "请输入大于 0 的整数"
                        } else {
                            "系统默认 DPI：$defaultDpi"
                        }
                    )
                },
                isError = dpi == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            OutlinedTextField(
                value = fontScaleText,
                onValueChange = { fontScaleText = it.trim() },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "应用内字体缩放")
                },
                supportingText = {
                    Text(
                        text = if (fontScale == null) {
                            "请输入大于 0 的数字"
                        } else {
                            "系统默认字体缩放：$defaultFontScale"
                        }
                    )
                },
                isError = fontScale == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )

            Button(
                onClick = {
                    if (dpi != null && fontScale != null) {
                        onConfirm(dpi, fontScale)
                    }
                },
                enabled = dpi != null && fontScale != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "确认修改")
            }
        }
    }
}
