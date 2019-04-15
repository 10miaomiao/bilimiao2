package com.a10miaomiao.bilimiao.entity.bangumi

import com.a10miaomiao.bilimiao.entity.*
import com.a10miaomiao.bilimiao.entity.Season

data class Bangumi(
        val cover: String,
        val episodes: List<BangumiEpisode>,
        val evaluate: String,
        val is_new_danmaku: Int,
        val link: String,
        val media_id: Int,
        val mid: Int,
        val mode: Int,
        val newest_ep: NewestEp,
        val publish: Publish,
        val rating: Rating,
        val record: String,
        val rights: Rights,
        val season_id: Int,
        val season_status: Int,
        val season_title: String,
        val season_type: Int,
        val seasons: List<BangumiSeason>,
        val series_id: Int,
        val share_url: String,
        val square_cover: String,
        val stat: StatX,
        val title: String,
        val total_ep: Int
)