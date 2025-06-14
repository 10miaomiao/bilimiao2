package cn.a10miaomiao.bilimiao.compose.pages.player

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavBackStackEntry
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.common.toPaddingValues
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.store.WindowStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class SendDanmakuPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: SendDanmakuViewModel = diViewModel()
        SendDanmakuPageContent(viewModel)
    }
}

internal data class SelectItemInfo<T>(
    val label: String,
    val value: T,
)

internal class SendDanmakuViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val pageNavigation by instance<PageNavigation>()
    private val playerDelegate by instance<BasePlayerDelegate>()

    val focusRequester = FocusRequester()

    internal val danmakuTypeList = listOf<SelectItemInfo<Int>>(
        SelectItemInfo("滚动", 1),
        SelectItemInfo("顶部", 5),
        SelectItemInfo("底部", 4)
    )

    internal val danmakuColorList = listOf<SelectItemInfo<Int>>(
        SelectItemInfo("#FFFFFF", 0xFFFFFF),
        SelectItemInfo("#FE0302", 0xFE0302),
        SelectItemInfo("#FF7204", 0xFF7204),
        SelectItemInfo("#FFAA02", 0xFFAA02),
        SelectItemInfo("#FFD302", 0xFFD302),
        SelectItemInfo("#FFFF00", 0xFFFF00),
        SelectItemInfo("#A0EE00", 0xA0EE00),
        SelectItemInfo("#00CD00", 0x00CD00),
        SelectItemInfo("#019899", 0x019899),
        SelectItemInfo("#4266BE", 0x4266BE),
        SelectItemInfo("#89D5FF", 0x89D5FF),
        SelectItemInfo("#CC0273", 0xCC0273),
        SelectItemInfo("#222222", 0x222222),
        SelectItemInfo("#9B9B9B", 0x9B9B9B),
    )

    internal val danmakuTextSizeList = listOf<SelectItemInfo<Float>>(
        SelectItemInfo("默认", 25f),
        SelectItemInfo("较小", 18f),
    )

    val loading = MutableStateFlow(false)
    val danmakuType = MutableStateFlow(1)
    val danmakuText = MutableStateFlow("")
    val danmakuColor = MutableStateFlow(0xFFFFFF)
    val danmakuTextSize = MutableStateFlow(25f)

    fun setDanmakuTextTypeValue(value: Int) {
        danmakuType.value = value
    }

    fun setDanmakuTextValue(value: String) {
        danmakuText.value = value
    }

    fun setDanmakuTextColorValue(value: Int) {
        danmakuColor.value = value
    }

    fun setDanmakuTextSizeValue(value: Float) {
        danmakuTextSize.value = value
    }

    fun sendDanmaku() {
        val text = danmakuText.value.replace("\n", " ")
        if (text.isBlank()) {
            PopTip.show("请输入弹幕内容")
            return
        }
        if (text.length > 50) {
            PopTip.show("弹幕内容字数过多")
            return
        }

        val type = danmakuType.value
        val color = danmakuColor.value
        val textSize = danmakuTextSize.value

        val sourceIds = playerDelegate.getSourceIds()
        val currentPosition = playerDelegate.currentPosition()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                loading.value = true
                val res = BiliApiService.playerAPI.sendDamaku(
                    aid = sourceIds.aid,
                    oid = sourceIds.cid,
                    msg = text,
                    mode = type,
                    fontsize = textSize.toInt(),
                    color = color,
                    progress = currentPosition,
                ).awaitCall().json<MessageInfo>()
                withContext(Dispatchers.Main) {
                    if (res.isSuccess) {
                        PopTip.show("发送成功")
                        playerDelegate.sendDanmaku(
                            type,
                            text,
                            textSize,
                            color,
                            currentPosition
                        )
                        pageNavigation.popBackStack()
                    } else {
                        PopTip.show(res.message)
                    }
                }
                loading.value = false
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    PopTip.show(e.message ?: e.toString())
                }
                loading.value = false
            }
        }
    }

    fun requestFocus() {
        focusRequester.requestFocus()
    }

    fun freeFocus() {
        focusRequester.freeFocus()
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun SendDanmakuPageContent(
    viewModel: SendDanmakuViewModel
) {
    PageConfig(
        title = "发送弹幕"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val loading = viewModel.loading.collectAsState().value
    val danmakuType = viewModel.danmakuType.collectAsState().value
    val danmakuText = viewModel.danmakuText.collectAsState().value
    val danmakuColor = viewModel.danmakuColor.collectAsState().value
    val danmakuTextSize = viewModel.danmakuTextSize.collectAsState().value

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.requestFocus()
    }

    Column(
        modifier = Modifier
            .padding(windowInsets.toPaddingValues())
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth()
                .focusRequester(viewModel.focusRequester),
            value = danmakuText,
            onValueChange = viewModel::setDanmakuTextValue,
            label = {
                Text(text = "弹幕内容")
            },
            trailingIcon = {
                Box(
                    modifier = Modifier.padding(end = 5.dp)
                ) {
                    Button(
                        modifier = Modifier.width(80.dp),
                        onClick = viewModel::sendDanmaku,
                        enabled = !loading,
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = "发送",
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "弹幕位置：",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(end = 10.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.danmakuTypeList) {
                    FilterChip(
                        selected = danmakuType == it.value,
                        onClick = {
                            viewModel.setDanmakuTextTypeValue(it.value)
                        },
                        label = {
                            Text(text = it.label)
                        }
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 默认为25,极小：12,超小：16,小：18,标准：25,大：36,超大：45,极大：64
            Text(
                "字体大小：",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(end = 10.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.danmakuTextSizeList) {
                    FilterChip(
                        selected = danmakuTextSize == it.value,
                        onClick = {
                            viewModel.setDanmakuTextSizeValue(it.value)
                        },
                        label = {
                            Text(text = it.label)
                        }
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "弹幕颜色：",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(end = 10.dp),
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.danmakuColorList) {
                    FilterChip(
                        selected = danmakuColor == it.value,
                        onClick = {
                            viewModel.setDanmakuTextColorValue(it.value)
                        },
                        label = {
                            Text(
                                text = it.label,
                                modifier = Modifier.background(Color(it.value.toLong() or 0xFF000000))
                            )
                        }
                    )
                }
            }
        }
    }
}