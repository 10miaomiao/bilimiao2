package com.a10miaomiao.bilimiao

import android.util.Base64
import com.a10miaomiao.bilimiao.netword.BiliApiService
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

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
}
