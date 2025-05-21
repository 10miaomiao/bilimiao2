package com.a10miaomiao.bilimiao.comm.miao

import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object MiaoJson {

    val kotlinJson = Json {
        ignoreUnknownKeys = true // 忽略未知 Key
        explicitNulls = true // 空值填充
        isLenient = true // 宽松校验
    }

    inline fun <reified T> fromJson(json: String): T {
        try {
            return kotlinJson.decodeFromString(json)
        } catch (e: IllegalStateException) {
            miaoLogger().i("JSON解析出错", json)
            throw e
        }
    }

    inline fun <reified T> toJson(value: T): String {
        return kotlinJson.encodeToString(value)
    }

}