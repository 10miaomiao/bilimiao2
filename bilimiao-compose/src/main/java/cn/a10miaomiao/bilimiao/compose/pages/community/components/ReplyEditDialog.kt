package cn.a10miaomiao.bilimiao.compose.pages.community.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AnyPopDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AnyPopDialogProperties
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AutoSheetDialog
import cn.a10miaomiao.bilimiao.compose.components.dialogs.DirectionState
import cn.a10miaomiao.bilimiao.compose.pages.community.ReplyEditParams
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmoteInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePackageInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePanelInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentSendResultInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Stable
class ReplyEditDialogState(
    val scope: CoroutineScope,
    val onAddReply: (VideoCommentReplyInfo) -> Unit,
) {

    private var replyParams: ReplyEditParams? = null

    private val _visible = mutableStateOf(false)
    val visible: Boolean get() = _visible.value

    private val _loading = mutableStateOf(false)
    val loading: Boolean get() = _loading.value

    val focusRequester =  FocusRequester()

    var _input = mutableStateOf(TextFieldValue(""))
    val input: TextFieldValue get() = _input.value

    val textEmpty: Boolean by derivedStateOf {
        input.text.isEmpty()
    }

    val snackbar = SnackbarHostState()

    fun show(params: ReplyEditParams) {
        replyParams = params
        _visible.value = true
    }

    fun dismiss() {
        replyParams = null
        _visible.value = false
    }

    fun inputChange(value: TextFieldValue) {
        _input.value = value

    }

    fun requestFocus() {
        focusRequester.requestFocus()
    }

    fun freeFocus() {
        focusRequester.freeFocus()
    }

    fun inputEmoji(emoji: UserEmoteInfo) {
        val originalText = input.text
        val position = input.selection.min + emoji.text.length
        _input.value = input.copy(
            text = originalText.substring(0, input.selection.min)
                    + emoji.text
                    + originalText.substring(input.selection.max),
            selection = TextRange(position)
        )
    }

    private suspend fun _sendReply() {
        try {
            val message = input.text
            val params = replyParams
            if (params == null) {
                snackbar.showSnackbar("参数错误")
                return
            }
            withContext(Dispatchers.Main) {
                _loading.value = true
            }
            val res = BiliApiService.commentApi
                .add(
                    message = if (params.parent != null
                        && params.parent != params.root) {
                        "回复 @${params.name} :$message"
                    } else {
                        message
                    },
                    type = params.type,
                    oid = params.oid,
                    root = params.root,
                    parent = params.parent,
                )
                .awaitCall()
                .json<ResponseData<VideoCommentSendResultInfo>>(isLog = true)
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    val result = res.requireData()
                    _visible.value = false
                    PopTip.show(result.success_toast)
                    _input.value = TextFieldValue("")
                    delay(1000L)
                    onAddReply(result.reply)
                } else {
                    snackbar.showSnackbar(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            snackbar.showSnackbar(e.message ?: e.toString())
        } finally {
            withContext(Dispatchers.Main) {
                _loading.value = false
            }
        }
    }
    fun sendReply() {
        if (loading) return
        scope.launch(Dispatchers.IO) {
            _sendReply()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReplyEditDialog(
    state: ReplyEditDialogState
) {
    val showEmojiGrid = remember { mutableStateOf(false) }
    if (state.visible) {
        AutoSheetDialog(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp),
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ReplyTextField(
                        state = state,
                    )
                    SnackbarHost(hostState = state.snackbar)
                    ReplyTextToolbar(
                        modifier = Modifier.padding(top = 5.dp),
                        visibleEmoji = showEmojiGrid.value,
                        loading = state.loading,
                        onEmojiClick = {
                            showEmojiGrid.value = !showEmojiGrid.value
                        },
                        onSendClick = state::sendReply
                    )
                    AnimatedVisibility(
                        visible = showEmojiGrid.value
                    ) {
                        EmojiGridBox(
                            modifier = Modifier.padding(top = 5.dp)
                                .height(emotePanelHeight),
                            onInputEmoji = {
                                state.inputEmoji(it)
                            }
                        )
                    }
                }
                val imeVisible = WindowInsets.Companion.isImeVisible
                val keyboardController = LocalSoftwareKeyboardController.current
                LaunchedEffect(imeVisible) {
                    if (showEmojiGrid.value && imeVisible) {
                        showEmojiGrid.value = false
                    }
                }
                LaunchedEffect(showEmojiGrid.value) {
                    if (showEmojiGrid.value && imeVisible) {
                        keyboardController?.hide()
                    } else if (!showEmojiGrid.value && !imeVisible) {
                        state.requestFocus()
                        keyboardController?.show()
                    }
                }
            },
            onDismiss = state::dismiss,
            onPreDismiss = {
                if (showEmojiGrid.value) {
                    showEmojiGrid.value = false
                    true
                } else {
                    false
                }
            }
        )
    }
}

private val circleButtonSize = 44.dp
private val minInputHeight = 90.dp
private val emotePanelHeight = 300.dp

@Composable
private fun ReplyTextField(
    modifier: Modifier = Modifier,
    state: ReplyEditDialogState,
) {
    LaunchedEffect(state.visible) {
        if (state.visible) {
            state.requestFocus()
        }
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minInputHeight)
                .padding(8.dp)
                .focusRequester(state.focusRequester),
            textStyle = TextStyle(
                fontSize = 18.sp
            ),
            value = state.input,
            onValueChange = state::inputChange,
            cursorBrush = SolidColor(Color(0xff00897B)),
            decorationBox = { innerTextField ->
                if (state.textEmpty) {
                    Text("请发表你的评论", fontSize = 18.sp)
                }
                innerTextField()
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    state.freeFocus()
                }
            ),
        )
    }
}


@Composable
private fun ReplyTextToolbar(
    modifier: Modifier = Modifier,
    visibleEmoji: Boolean,
    loading: Boolean,
    onEmojiClick: () -> Unit,
    onSendClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        TextButton(
            onClick = onEmojiClick,
            modifier = Modifier.then(Modifier.size(circleButtonSize)),
        ) {
            Icon(
                imageVector = Icons.Default.Mood,
                contentDescription = "emoji表情",
                tint = if (visibleEmoji) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
            )
        }
//        TextButton(
//            onClick = { /*TODO*/ },
//            modifier = Modifier.then(Modifier.size(circleButtonSize)),
//        ) {
//            Icon(
//                modifier = Modifier.rotate(-45f),
//                imageVector = Icons.Default.AttachFile,
//                contentDescription = "attach"
//            )
//        }
        Spacer(Modifier.weight(1f))
        FloatingActionButton(
            modifier = Modifier.height(circleButtonSize),
            onClick = onSendClick
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        "发布",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

