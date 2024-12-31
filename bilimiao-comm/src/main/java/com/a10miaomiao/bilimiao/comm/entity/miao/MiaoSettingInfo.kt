package com.a10miaomiao.bilimiao.comm.entity.miao

import kotlinx.serialization.Serializable


/**
 * {“name”: "donate", "type": "pref", "title": "捐助",
 * "summary": "我在这里哦。",
 * "url": "alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/FKX07587MLQPOBBKACENE1",
 * "backupUrl": "https://qr.alipay.com/FKX07587MLQPOBBKACENE1"
 * }
 * {“name”: "help", "type": "pref", "title": "帮助",
 * "summary": "世界太大，只能不停寻找。",
 * "url": "https://10miaomiao.cn/bilimiao/help.html"
 * }
 */
/**
 * 设置菜单
 */
@Serializable
data class MiaoSettingInfo(
    val type: String,
    val name: String,
    val title: String,
    val summary: String,
    val url: String,
    val backupUrl: String? = null,
)