package cn.a10miaomiao.bilimiao.compose.pages.home

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoAdInfo
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger

@Stable
class HomePageState(
    val pageNavigation: PageNavigation
) {

//    private val _title = mutableStateOf("时光姬")
    private val _adInfo = mutableStateOf<MiaoAdInfo.AdBean?>(null)
    val adInfo: State<MiaoAdInfo.AdBean?> get() = _adInfo

    private val _forceVisible = mutableStateOf(true)
    val forceVisible: State<Boolean> get() = _forceVisible

    fun setAdInfo(data: MiaoAdInfo.AdBean?) {
        _adInfo.value = data
    }

    fun setForceVisible(visible: Boolean) {
        _forceVisible.value = visible
    }

    fun openLinkUrl() {
        val url = adInfo.value?.link?.url ?: return
        if (!BilibiliNavigation.navigationTo(pageNavigation, url)) {
            BilibiliNavigation.navigationToWeb(pageNavigation, url)
        }
    }

}