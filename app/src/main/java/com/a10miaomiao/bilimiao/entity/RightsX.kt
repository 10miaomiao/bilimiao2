package com.a10miaomiao.bilimiao.entity

data class RightsX(
        val allow_bp: Int,
        val allow_download: Int,
        val allow_review: Int,
        val area_limit: Int,
        val ban_area_show: Int,
        val copyright: String,
        val is_preview: Int,
        val watch_platform: Int
)