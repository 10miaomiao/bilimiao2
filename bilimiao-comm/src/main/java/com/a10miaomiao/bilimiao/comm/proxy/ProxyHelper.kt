package com.a10miaomiao.bilimiao.comm.proxy

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object ProxyHelper {

    const val UPDATE_ACTION = "com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper.UPDATE"
    private const val jsonFileName = "/proxy_server_list.json"

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
        val jsonStr = Gson().toJson(list)
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
            val list = Gson().fromJson<List<ProxyServerInfo>>(
                jsonStr,
                object : TypeToken<List<ProxyServerInfo>>() {}.type,
            )
            list.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
            mutableListOf()
        }
    }

}