package com.a10miaomiao.bilimiao.page.search.result

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.RegionStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.collections.forEachWithIndex
import splitties.toast.toast
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class VideoRegionViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val regionStore: RegionStore by instance()

    val checkRegionId by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }

    val allRegionInfo = RegionInfo(
        tid =0,
        reid = 0,
        logo = null,
        icon = null,
        name = "全部分区",
        type = 0,
        uri = "",
        children = listOf()
    )

    val regionList get() = mutableListOf<RegionInfo>(
        allRegionInfo,
        *regionStore.state.regions.toTypedArray()
    )




}