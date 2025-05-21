package cn.a10miaomiao.bilimiao.compose.pages.video.components

import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.common.foundation.annotatedText
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AutoSheetDialog
import cn.a10miaomiao.bilimiao.compose.pages.download.DownloadBangumiCreatePageViewModel.QualityInfo
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
class VideoCoinDialogState(
    val scope: CoroutineScope,
    val onChanged: (Int) -> Unit,
) {

    private var aid = ""

    private val _visible = mutableStateOf(false)
    val visible: Boolean get() = _visible.value

    private val _loading = mutableStateOf(false)
    val loading: Boolean get() = _loading.value

    private val _coinNum = mutableStateOf(0)
    val coinNum: Int get() = _coinNum.value

    private val _maxCoinNum = mutableStateOf(0)
    val maxCoinNum: Int get() = _maxCoinNum.value

    val snackbar = SnackbarHostState()

    fun requestCoin(num: Int) = scope.launch(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) {
                _loading.value = true
            }
            val res = BiliApiService.videoAPI
                .coin(aid, num)
                .awaitCall()
                .json<MessageInfo>()
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    PopTip.show("感谢投币")
                    dismiss()
                    onChanged(num)
                } else {
                    snackbar.showSnackbar(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                snackbar.showSnackbar(e.message ?: e.toString())
            }
        } finally {
            withContext(Dispatchers.Main) {
                _loading.value = false
            }
        }
    }

    fun confirmCoin() {
        requestCoin(coinNum)
    }

    fun setCoinNum(num: Int) {
        _coinNum.value = num
    }

    fun show(videoAid: String, copyright: Int) {
        aid = videoAid
        _maxCoinNum.value = if (copyright == 2) 1 else 2
        _coinNum.value = maxCoinNum
        _visible.value = true
    }

    fun dismiss() {
        _visible.value = false
    }
}

@Composable
fun VideoCoinRadioButton(
    modifier: Modifier = Modifier,
    num: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ElevatedAssistChip(
        onClick = onClick,
        modifier = modifier,
        label = {
            Box(
                modifier = Modifier
                    .heightIn(min = 40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("给UP主投上 ")
                        withStyle(
                            style = SpanStyle(
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        ) {
                            append(num.toString())
                        }
                        append(" 枚硬币")
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        leadingIcon = {
            RadioButton(
                selected = selected,
                onClick = onClick // null recommended for accessibility with screen readers
            )
        }
    )
}

@Composable
fun VideoCoinDialog(
    state: VideoCoinDialogState
) {
    if (state.visible) {
        AutoSheetDialog(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .heightIn(max = 400.dp),
            content = {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "请选择投币",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            VideoCoinRadioButton(
                                num = 1,
                                selected = state.coinNum == 1,
                                onClick = {
                                    state.setCoinNum(1)
                                }
                            )
                            if (state.maxCoinNum > 1) {
                                Spacer(modifier = Modifier.height(20.dp))
                                VideoCoinRadioButton(
                                    num = 2,
                                    selected = state.coinNum == 2,
                                    onClick = {
                                        state.setCoinNum(2)
                                    }
                                )
                            }
                        }
                        SnackbarHost(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            hostState = state.snackbar,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(
                                vertical = 5.dp,
                                horizontal = 10.dp
                            )
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = state::confirmCoin,
                            enabled = !state.loading
                        ) {
                            Row {
                                if (state.loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(end = 5.dp),
                                        strokeWidth = 2.dp,
                                    )
                                }
                                Text(text = "确定")
                            }
                        }
                    }
                }
            },
            onDismiss = state::dismiss
        )
    }
}