package com.a10miaomiao.bilimiao.page.video

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class VideoInfoViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

    init {

    }

}