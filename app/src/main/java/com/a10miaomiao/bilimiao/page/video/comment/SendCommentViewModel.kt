package com.a10miaomiao.bilimiao.page.video.comment

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmoteInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePackageInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UserEmotePanelInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentSendResultInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.kongzue.dialogx.dialogs.PopTip
import com.kongzue.dialogx.dialogs.TipDialog
import com.kongzue.dialogx.dialogs.WaitDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class SendCommentViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    var isShowEmoteGrid = false // 显示表情输入框
    var isShowSoftInput = false // 显示软键盘
    val emotePackageList = mutableListOf<UserEmotePackageInfo>()
    val currentEmotePackage = MutableStateFlow<UserEmotePackageInfo?>(null)

    val params by lazy {
        fragment.requireArguments().getParcelable<SendCommentParam>(MainNavArgs.params)!!
    }
//    val emoteList = mutableListOf<UserEmoteInfo>()
//    var currentSelectPackage: UserEmotePackageInfo? = null
//        set(value) {
//            field = value
//            emoteList.clear()
//            value?.let {
//                emoteList.addAll(value.emote)
//            }
//        }


    init {
        getUserEmoteList()
    }

    private fun getUserEmoteList() = viewModelScope.launch {
        try {
            val res = BiliApiService.commentApi
                .emoteList()
                .awaitCall()
                .gson<ResultInfo<UserEmotePanelInfo>>()
            if (res.isSuccess) {
                val result = res.data
                ui.setState {
                    emotePackageList.clear()
                    emotePackageList.addAll(result.packages)
                    if (emotePackageList.isNotEmpty()) {
                        currentEmotePackage.value = emotePackageList[0]
//                        currentSelectPackage = emotePackageList[0]
                    }
//                    DebugMiao.log(emoteList)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendComment(
        message: String,
    ) = viewModelScope.launch {
        try {
            withContext(Dispatchers.Main) {
                WaitDialog.show("发送中")
            }
            val res = BiliApiService.commentApi
                .add(
                    message = if (params.type == 3) {
                        "回复 ${params.name} :$message"
                    } else {
                        message
                    },
                    oid = params.oid,
                    root = params.root,
                    parent = params.parent,
                )
                .awaitCall()
                .gson<ResultInfo<VideoCommentSendResultInfo>>(isDebug = true)
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    val result = res.data
                    val navController = fragment.findNavController()
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        MainNavArgs.reply, result.reply
                    )
                    navController.popBackStack()
                    TipDialog.show(result.success_toast, WaitDialog.TYPE.SUCCESS)
                } else {
                    TipDialog.show(res.message, WaitDialog.TYPE.WARNING)
                }
            }
        } catch (e: Exception) {
            DebugMiao.log(e)
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                TipDialog.show("发送失败", WaitDialog.TYPE.ERROR)
//                PopTip.show(e.message ?: e.toString())
            }
        }
    }

    fun setSelectPackage(selectPackage: UserEmotePackageInfo) {
        currentEmotePackage.value = selectPackage
//        ui.setState {
//            currentSelectPackage = selectPackage
//        }
    }

    fun setEmoteGridDisplay(show: Boolean) {
        ui.setState {
            isShowEmoteGrid = show
        }
    }

    fun setSoftInputDisplay(show: Boolean) {
        ui.setState {
            isShowSoftInput = show
        }
    }
}