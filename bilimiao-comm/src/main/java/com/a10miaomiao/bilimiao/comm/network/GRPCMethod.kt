package com.a10miaomiao.bilimiao.comm.network

import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GRPCMethod<ReqT : pbandk.Message, RespT : pbandk.Message>(
    val name: String,
    val reqMessage: ReqT,
    val respMessageCompanion: pbandk.Message.Companion<RespT>,
)