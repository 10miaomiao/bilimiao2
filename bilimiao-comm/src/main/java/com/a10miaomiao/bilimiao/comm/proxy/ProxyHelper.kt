package com.a10miaomiao.bilimiao.comm.proxy

import android.content.Context
import android.content.Intent
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import kotlinx.serialization.encodeToString
import java.io.File

object ProxyHelper {

    const val UPDATE_ACTION = "com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper.UPDATE"
    private const val jsonFileName = "/proxy_server_list.json"
    private const val KEY_UPOS = "proxy_upos"

    var version = 0

    fun saveServer(
        context: Context,
        server: ProxyServerInfo?,
        index: Int = -1,
    ) {
        val list = serverList(context)
        if (index in 0 until list.size) {
            if (server == null) {
                list.removeAt(index)
            } else {
                list[index] = server
            }
        } else if (server != null) {
            list.add(server)
        }
        val jsonStr = MiaoJson.toJson(list)
        val file = File(context.filesDir.path + jsonFileName)
        file.writeText(jsonStr)
        // 通知更新
        context.sendBroadcast(Intent(UPDATE_ACTION))
        version++
    }

    fun serverList(
        context: Context
    ): MutableList<ProxyServerInfo> {
        val file = File(context.filesDir.path + jsonFileName)
        if (file.isDirectory) {
            file.delete()
        }
        if (!file.exists()) {
            return mutableListOf()
        }
        val jsonStr = file.readText()
        return try {
            val list = MiaoJson.fromJson<List<ProxyServerInfo>>(jsonStr)
            list.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

    fun saveUposName(
        context: Context,
        uposName: String,
    ) {
        val sp = context.getSharedPreferences(BilimiaoCommApp.APP_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(KEY_UPOS, uposName).apply()
    }

    fun uposName(context: Context): String {
        val sp = context.getSharedPreferences(BilimiaoCommApp.APP_NAME, Context.MODE_PRIVATE)
        return sp.getString(KEY_UPOS, "none")!!
    }

}