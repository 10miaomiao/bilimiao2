package cn.a10miaomiao.bilimiao.compose.common.constant

object PageTabIds {

    const val HomeTimeMachine = "home.time-machine"
    const val HomeRecommend = "home.recommend"
    const val HomePopular = "home.popular"

    const val DynamicAll = "dynamic.all"
    const val DynamicVideo = "dynamic.video"
    val DynamicByUpper = TabId("dynamic.upper")

    const val SearchAll = "search.all"
    val SearchByType = TabId("search.type")

    const val UserIndex = "user.index"
    const val UserDynamic = "user.dynamic"
    const val UserArchive = "user.archive"
    const val UserSearchArchive = "user.search-archive"
    const val UserSearchDynamic = "user.search-dynamic"

    val MyBangumi = TabId("my.bangumi")

    class TabId(
        private val name: String
    ) {
        operator fun get(key: Int): String {
            return "$name[$key]"
        }
        operator fun get(key: String): String {
            return "$name[$key]"
        }
    }
}