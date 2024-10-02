package cn.a10miaomiao.bilimiao.compose.components.preference

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.a10miaomiao.bilimiao.comm.utils.GlideCacheUtil
import com.kongzue.dialogx.dialogs.PopTip
import me.zhanghai.compose.preference.Preference

inline fun LazyListScope.glidePreference(
    key: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    item(key = key, contentType = "GlidePreference") {
        GlidePreference(modifier = modifier)
    }
}

@Composable
fun GlidePreference(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var cacheSize by remember {
        val cache = GlideCacheUtil.getCacheSize(context)
        mutableStateOf(cache)
    }
    var showDialog by remember {
        mutableStateOf(false)
    }

    Preference(
        modifier = modifier,
        title = {
            Text("图片缓存")
        },
        summary = {
            Text(cacheSize)
        },
        onClick = {
            showDialog = true
        }
    )

    if (showDialog) {
        AlertDialog(
            title = {
                Text("提示")
            },
            text = {
                Text("确定清空图片缓存？当前缓存大小：$cacheSize")
            },
            onDismissRequest = {
                showDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        GlideCacheUtil.clearImageAllCache(context)
                        PopTip.show("清理完成，已清理$cacheSize")
                        cacheSize = "0Byte"
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}