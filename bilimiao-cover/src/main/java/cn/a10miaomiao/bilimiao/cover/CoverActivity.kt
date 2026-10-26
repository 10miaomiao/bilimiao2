package cn.a10miaomiao.bilimiao.cover

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.a10miaomiao.bilimiao.compose.BilimiaoTheme
import cn.a10miaomiao.bilimiao.compose.platform.AndroidPlatformContext
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance


class CoverActivity : ComponentActivity(), DIAware {

    companion object {
        fun launch(context: Context, id: String, type: String) {
            val mIntent = Intent(context, CoverActivity::class.java)
            mIntent.putExtra("id", id)
            mIntent.putExtra("type", type)
            context.startActivity(mIntent)
        }

        fun launch(context: Context, id: String) {
            if (id.startsWith("BV")) {
                launch(context, id, "BV")
            } else {
                launch(context, id, "AV")
            }
        }
    }

    private lateinit var viewModel: CoverViewModel

    override val di: DI = DI.lazy {
        bindSingleton { AppStore(di) }
    }

    // 封面页独立于主界面组合，拿不到主界面的 Store，自行注入 AppStore 以读取应用的主题设置
    private val appStore: AppStore by di.instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CoverViewModel(this@CoverActivity) as T
                }
            },
        )[CoverViewModel::class.java]
        appStore.init()
        initArgument()
        setContent {
            val appState by appStore.stateFlow.collectAsState()
            // 跟随应用的主题设置（主题色、明暗模式）
            BilimiaoTheme(appState = appState) {
                CompositionLocalProvider(
                    LocalPlatformContext provides AndroidPlatformContext(this@CoverActivity)
                ) {
                    CoverSheet(
                        viewModel = viewModel,
                        onClose = { finish() },
                    )
                }
            }
        }
    }

    private fun initArgument() {
        val extras = intent.extras ?: return
        viewModel.setConfig("", "少女正在祈祷")
        if (extras.containsKey(Intent.EXTRA_TEXT)) {
            val text = extras.getString(Intent.EXTRA_TEXT)!!
            val urlInfo = BiliUrlMatcher.findIDByUrl(text)
            val type = urlInfo[0].uppercase()
            val id = urlInfo[1]
            if (type == "未知类型") {
                val textList = text.split(" ")
                if (textList.size > 1) {
                    val url = textList[textList.size - 1]
                    if (url.indexOf("https://") == 0 || url.indexOf("http://") == 0) {
                        viewModel.resolveUrl(url)
                    }
                }
            } else {
                viewModel.setConfig(type, id)
            }
        } else if (extras.containsKey("id") && extras.containsKey("type")) {
            val type = extras.getString("type")!!.uppercase()
            val id = extras.getString("id")!!
            viewModel.setConfig(type, id)
        } else if (extras.containsKey("id")) {
            // 从视频详情页进入时只带 id，按 BV/AV 规则推断类型
            val id = extras.getString("id")!!
            if (id.startsWith("BV")) {
                viewModel.setConfig("BV", id.substring(2))
            } else {
                viewModel.setConfig("AV", id)
            }
        } else {
            viewModel.setConfig("", "未知")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
