package com.a10miaomiao.bilimiao

import com.a10miaomiao.bilimiao.netword.BiliApiService
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        var pageNum = 1
        val pageSize = 10
        var rankOrder = "click"  //排行依据
        val rankOrdersValueList = arrayListOf("default", "ranklevel", "pubdate", "click", "dm", "scores", "stow")
        val regionValueList = arrayListOf(0, 13, 167, 1,
                3, 129, 4, 36,
                160, 119, 155, 165,
                5, 23, 11)
        val duration = 0
        val region = regionValueList[0]
        println(BiliApiService.getSearchArchive("2333", pageNum, pageSize, rankOrder, duration, region))
        assertEquals(4, 2 + 2)
    }

    @Test
    fun av(){
//        val content = """
//            第一版做完后因为没法去录屏浮窗+乱搞帖纸+烧酒不够导致后半段几乎全用动漫填充所以一直想做的第二版来啦
//            第一版：av53794441
//            在动画上映吸引大佬来前炫耀一波自己的菜鸡作品
//            第一版：av53794441
//            BV1cJ41147op
//        """.trimIndent()
//        var result = "av(\\d+)".toRegex().replace(content, "[$0](https://www.bilibili.com/video/av$1)")
//        result = "BV([a-zA-Z0-9]+)".toRegex().replace(result, "[$0](https://www.bilibili.com/video/BV$1)")
//        println(result)
        var url = BiliApiService.getRegion()
        println(url)
        assertEquals(4, 2 + 2)
    }
}
