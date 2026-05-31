package com.a10miaomiao.bilimiao.activity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.ScreenDpiUtil
import com.a10miaomiao.bilimiao.config.config
import com.kongzue.dialogx.dialogs.PopTip

class DensitySettingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DensitySettingScreen(
                onBack = { onBackPressedDispatcher.onBackPressed() },
                onConfirm = { dpi, fontScale -> setCustomConfiguration(dpi, fontScale) }
            )
        }
    }

    override fun onBackPressed() {
        if (isChanged()) {
            val intent = packageManager.getLaunchIntentForPackage(packageName)!!
            val componentName = intent.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            startActivity(mainIntent)
            Runtime.getRuntime().exit(0)
        } else {
            finish()
        }
    }

    fun setCustomConfiguration(dpi: Int, fontScale: Float) {
        if (dpi <= 0 || fontScale <= 0f) {
            PopTip.show("请输入大于0的整数")
        }
        ScreenDpiUtil.saveCustomConfiguration(dpi, fontScale)
        reStartActivity()
    }

    private fun reStartActivity() {
        val intent = intent
        intent.putExtra("changed", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    private fun isChanged(): Boolean {
        return intent.getBooleanExtra("changed", false)
    }

    override fun attachBaseContext(newBase: Context) {
        val configuration: Configuration = newBase.resources.configuration
        ScreenDpiUtil.readCustomConfiguration(configuration)
        val newContext = newBase.createConfigurationContext(configuration)
        super.attachBaseContext(newContext)
    }
}

@Composable
private fun DensitySettingScreen(
    onBack: () -> Unit,
    onConfirm: (Int, Float) -> Unit,
) {
    val context = LocalContext.current
    val bgColor = remember { context.config.windowBackgroundColor }
    val defaultDpi = remember { ScreenDpiUtil.getDefaultDpi() }
    val defaultFontScale = remember { ScreenDpiUtil.getDefaultFontScale() }
    val currentDpi = remember { context.resources.configuration.densityDpi.toString() }
    val currentFontScale = remember { context.resources.configuration.fontScale.toString() }
    val dpiText = remember { mutableStateOf(currentDpi) }
    val fontScaleText = remember { mutableStateOf(currentFontScale) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.density_setting)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(bgColor))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "系统默认DPI：$defaultDpi", fontSize = 14.sp)
            Text(text = "当前应用内DPI修改：", fontSize = 14.sp)
            OutlinedTextField(
                value = dpiText.value,
                onValueChange = { dpiText.value = it },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "系统默认字体缩放：$defaultFontScale", fontSize = 14.sp)
            Text(text = "当前字体缩放修改：", fontSize = 14.sp)
            OutlinedTextField(
                value = fontScaleText.value,
                onValueChange = { fontScaleText.value = it },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    try {
                        val dpi = dpiText.value.toInt()
                        val fontScale = fontScaleText.value.toFloat()
                        onConfirm(dpi, fontScale)
                    } catch (ex: NumberFormatException) {
                        PopTip.show("请输入整数")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "确认修改")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
