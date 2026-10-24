package com.a10miaomiao.bilimiao.comm.proxy

data class BiliUposInfo(
    val name: String,
    val label: String,
    val host: String,
) {
    companion object {

        /**
         * 可互相替换的 UPOS 视频服务器
         *
         * B 站会把同一份分片文件放到多个 UPOS 镜像上，且镜像之间通用同一套签名，
         * 所以把播放地址的 host 换掉就能从另一个镜像取到同一份数据。
         *
         * 注意 Akamai（`upos-hz-mirrorakam.akamaized.net`）不在「可随意替换」之列：
         * 它只接受 B 站为它自己下发的地址，直接替换 host 会被拒。智能换源
         * （SmartSourceSelector）据此把它排除在 host 替换的候选之外。
         *
         * `none` 表示不替换，始终排在首位。
         */
        val PRESETS = listOf(
            BiliUposInfo("none", "不替换", ""),
            BiliUposInfo("ali", "ali（阿里云）", "upos-sz-mirrorali.bilivideo.com"),
            BiliUposInfo("cos", "cos（腾讯云）", "upos-sz-mirrorcos.bilivideo.com"),
            BiliUposInfo("hw", "hw（华为云）", "upos-sz-mirrorhw.bilivideo.com"),
            BiliUposInfo("akamai", "akamai（Akamai海外）", "upos-hz-mirrorakam.akamaized.net"),
            BiliUposInfo("aliov", "aliov（阿里海外）", "upos-sz-mirroraliov.bilivideo.com"),
            BiliUposInfo("cosov", "cosov（腾讯海外）", "upos-sz-mirrorcosov.bilivideo.com"),
            BiliUposInfo("tf_hw", "tf_hw（华为）", "upos-tf-all-hw.bilivideo.com"),
            BiliUposInfo("tf_tx", "tf_tx（腾讯）", "upos-tf-all-tx.bilivideo.com"),
        )

        /** 按 name 反查。设置里存的是 name，取 host 时需要先翻译一次 */
        fun findByName(name: String): BiliUposInfo? = PRESETS.firstOrNull { it.name == name }
    }
}
