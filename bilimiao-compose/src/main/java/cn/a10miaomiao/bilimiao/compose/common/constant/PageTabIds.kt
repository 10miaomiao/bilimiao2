package cn.a10miaomiao.bilimiao.compose.common.constant

object PageTabIds {

    const val HomeTimeMachine = "home.time-machine"
    const val HomeRecommend = "home.recommend"
    const val HomePopular = "home.popular"

    const val DynamicAll = "dynamic.all"
    const val DynamicVideo = "dynamic.video"

    const val SearchAll = "search.all"
    val SearchByType = TabId("search.type")

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