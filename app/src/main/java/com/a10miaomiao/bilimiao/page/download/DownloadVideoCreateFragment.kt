package com.a10miaomiao.bilimiao.page.download

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.template.TemplateViewModel
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.views.dsl.core.verticalLayout

class DownloadVideoCreateFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "创建下载任务"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<DownloadVideoCreateViewModel>(di)

    private val windowStore by instance<WindowStore>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    val ui = miaoBindingUi {
        verticalLayout {

        }
    }

}