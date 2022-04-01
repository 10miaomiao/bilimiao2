package com.a10miaomiao.bilimiao.page.bangumi

import android.content.Context
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class BangumiPagesViewModel (
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

}