package cn.a10miaomiao.bilimiao.compose.pages.video.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AutoSheetDialog
import cn.a10miaomiao.bilimiao.compose.pages.community.ReplyEditParams
import cn.a10miaomiao.bilimiao.compose.pages.download.EpisodeItem
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.EpisodeInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaResponseInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Stable
class VideoAddFavoriteDialogState(
    val scope: CoroutineScope,
    val onChanged: (Int) -> Unit,
) {

    var aid: String = ""
        private set

    private val _visible = mutableStateOf(false)
    val visible: Boolean get() = _visible.value

    private val _loading = mutableStateOf(false)
    val loading: Boolean get() = _loading.value

    private val _list = mutableStateListOf<MediaListInfo>()
    val list: List<MediaListInfo> get() = _list

    private val _listLoading = mutableStateOf(false)
    val listLoading: Boolean get() = _listLoading.value

    private val _listFail = mutableStateOf("")
    val listFail: String get() = _listFail.value

    private val _selectedMap = mutableStateMapOf<String, Boolean>()
    val selectedMap: Map<String, Boolean> get() = _selectedMap

    val snackbar = SnackbarHostState()

    suspend fun loadData() {
        try {
            withContext(Dispatchers.Main) {
                _listLoading.value = true
                _listFail.value = ""
            }
            val res = BiliApiService.videoAPI
                .favoriteCreated(aid)
                .awaitCall()
                .json<ResponseData<MediaResponseInfo>>()
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    _list.addAll(res.requireData().list)
                } else {
                    _listFail.value = res.message
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                _listFail.value = e.message ?: e.toString()
            }
        } finally {
            withContext(Dispatchers.Main) {
                _listLoading.value = false
            }
        }
    }

    private fun requestFavorite(
        favIds: List<String>,
        addIds: List<String>,
        delIds: List<String>,
    ) = scope.launch(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) {
                _loading.value = true
            }
            val res = BiliApiService.videoAPI
                .favoriteDeal(
                    aid = aid,
                    addIds = addIds,
                    delIds = delIds,
                )
                .awaitCall()
                .json<MessageInfo>()
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    if (favIds.size - delIds.size + addIds.size == 0) {
                        onChanged(0)
                    } else if (favIds.isEmpty()) {
                        onChanged(1)
                    }
                    aid = ""
                    PopTip.show("操作成功")
                    dismiss()
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

    fun confirmFavorite() {
        val favIds = list
            .filter { it.fav_state == 1 }
            .map { it.id }
        val addIds = list
            .filter { it.fav_state != 1 && (selectedMap[it.id] ?: false) }
            .map { it.id }
        val delIds = list
            .filter { it.fav_state == 1 && !(selectedMap[it.id] ?: true) }
            .map { it.id }
        requestFavorite(favIds, addIds, delIds)
    }

    fun show(videoAid: String) {
        if (videoAid != aid || list.isEmpty()) {
            scope.launch {
                loadData()
            }
            aid = videoAid
        }
        _visible.value = true
    }

    fun checkedChange(key: String, isChecked: Boolean) {
        _selectedMap[key] = isChecked
    }

    fun dismiss() {
        _visible.value = false
    }

}

@Composable
private fun VideoFavoriteItem(
    title: String,
    count: Int,
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
) {
    Box(
        modifier = Modifier
            .padding(
                vertical = 5.dp,
                horizontal = 10.dp,
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${count}个内容",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}

@Composable
fun VideoAddFavoriteDialog(
    state: VideoAddFavoriteDialogState,
) {
    if (state.visible) {
        AutoSheetDialog(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .heightIn(max = 500.dp),
            content = {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "请选择收藏夹",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            items(state.list, { it.id }) { item ->
                                val isChecked = if (item.fav_state == 1) {
                                    state.selectedMap[item.id] ?: true
                                } else {
                                    state.selectedMap[item.id] ?: false
                                }
                                VideoFavoriteItem(
                                    title = item.title,
                                    count = item.media_count,
                                    checked = isChecked,
                                    onCheckedChange = { state.checkedChange(item.id, it) }
                                )
                            }
                        }
                        if (state.list.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                if (state.listLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp,
                                    )
                                    Text(
                                        "加载中",
                                        modifier = Modifier.padding(start = 5.dp),
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 14.sp,
                                    )
                                } else if (state.listFail.isNotBlank()) {
                                    Text(
                                        state.listFail,
                                        modifier = Modifier.padding(start = 5.dp),
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 14.sp,
                                    )
                                }
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
                            onClick = state::confirmFavorite,
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
                                Text(text = "完成")
                            }
                        }
                    }
                }
            },
            onDismiss = state::dismiss
        )
    }
}