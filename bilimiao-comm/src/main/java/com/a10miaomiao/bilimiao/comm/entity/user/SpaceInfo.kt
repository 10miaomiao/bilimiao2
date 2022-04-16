package com.a10miaomiao.bilimiao.comm.entity.user

data class SpaceInfo(
    var card: CardInfo,
    var live: LiveInfo,
    var images: ImagesInfo,
    var favourite: Media<FavouriteItem>,
    var season: Media<SeasonItem>,
    var archive: Media<ArchiveItem>,
    var coin_archive: Media<ArchiveItem>,
    var like_archive: Media<ArchiveItem>,
    var tab: Tab
){
    data class CardInfo(
        val approve: Boolean,
        val article: Int,
        val attention: Int,
        val attentions: Any,
        val birthday: String,
        val description: String,
        val face: String,
        val fans: Int,
        val friend: Int,
        val level_info: LevelInfo,
        val mid: String,
        val name: String,
        val place: String,
        val rank: String,
        val regtime: Int,
        val relation: RelationInfo,
        val sex: String,
        val sign: String,
        val spacesta: Int
    )

    data class RelationInfo(
        val status: Int,
        var is_follow: Int,
    )

    data class LevelInfo(
        val current_exp: Int,
        val current_level: Int,
        val current_min: Int,
        val next_exp: String
    )

    data class ImagesInfo(
        val imgUrl: String
    )

    data class LiveInfo(
        val url: String,
        val title: String,
        val cover: String,
        val roomid: Long
    )

    data class Tab(
        val archive: Boolean,
        val favorite: Boolean,
        val bangumi: Boolean,
        val like: Boolean
    )

    data class Media<T>(
        var count: Int,
        var item: List<T>
    )

    data class FavouriteItem(
        val atten_count: Int,
        val cover: List<FavouriteItemCover>,
        val ctime: Int,
        val cur_count: Int,
        val fid: Long,
        val max_count: Int,
        val media_id: Long,
        val mid: Long,
        val mtime: Long,
        val name: String,
        val state: Int
    )

    data class FavouriteItemCover(
        val aid: Int,
        val pic: String,
        val type: Int
    )

    data class ArchiveItem(
        val cover: String,
        val ctime: Int,
        val danmaku: Int,
        val duration: Int,
        val goto: String,
        val length: String,
        val `param`: String,
        val play: Int,
        val state: Boolean,
        val title: String,
        val tname: String,
        val ugc_pay: Int,
        val uri: String
    )

    data class SeasonItem(
        val attention: String,
        val cover: String,
        val finish: Int,
        val goto: String,
        val index: String,
        val is_finish: String,
        val is_started: Int,
        val mtime: Int,
        val newest_ep_id: String,
        val newest_ep_index: String,
        val `param`: String,
        val title: String,
        val total_count: String,
        val uri: String
    )
}