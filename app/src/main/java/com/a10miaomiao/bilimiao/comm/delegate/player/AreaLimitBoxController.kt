package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.player.model.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.proxy.ProxyHelper
import com.a10miaomiao.bilimiao.comm.proxy.ProxyServerInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.page.setting.DanmakuSettingFragment
import org.kodein.di.DI
import org.kodein.di.DIAware

class AreaLimitBoxController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    override val di: DI,
) : DIAware, BroadcastReceiver() {

    val areaLimitLayout = activity.findViewById<RelativeLayout>(R.id.area_limit_layout)
    val proxySpinner = activity.findViewById<Spinner>(R.id.proxy_spinner)
    val proxySetBtn = activity.findViewById<TextView>(R.id.proxy_set_btn)
    val areaLimitOkBtn = activity.findViewById<View>(R.id.area_limit_ok_btn)
    val areaLimitCloseBtn = activity.findViewById<View>(R.id.area_limit_close_btn)


    private val adapter = ArrayAdapter<String>(
        activity,
        R.layout.item_proxy_spinner,
        mutableListOf()
    ).apply {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }
    private var serverList = emptyList<ProxyServerInfo>()
        set(value) {
            field = value
            adapter.clear()
            adapter.addAll(value.map { it.name })
        }
    private var selectedServer: ProxyServerInfo? = null
    private var playerSource: BangumiPlayerSource? = null

    init {
        initAreaLimitBox()
    }

    /**
     * 区域限制
     */
    private fun initAreaLimitBox() {
        proxySpinner.adapter = adapter
        proxySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedServer = serverList[p2]
            }
        }
        proxySetBtn.setOnClickListener {
            if (delegate.scaffoldApp.fullScreenPlayer) {
                delegate.controller.smallScreen()
            }
            val nav = activity.findNavController(R.id.nav_host_fragment)
            val url = "bilimiao://setting/proxy"
            nav.navigate(
                MainNavGraph.action.global_to_compose, bundleOf(
                    MainNavGraph.args.url to url
                )
            )
        }
        areaLimitOkBtn.setOnClickListener {
            if (selectedServer == null) {
                Toast.makeText(activity, "请选择代理服务器", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            playerSource?.let {
                it.proxyServer = selectedServer
                delegate.openPlayer(it)
            } ?: Toast.makeText(activity, "此视频不支持代理", Toast.LENGTH_SHORT).show()
        }
        areaLimitCloseBtn.setOnClickListener {
            hide()
            delegate.closePlayer()
        }
    }


    fun show(source: BangumiPlayerSource) {
        playerSource = source
        areaLimitLayout.visibility = View.VISIBLE
        delegate.views.videoPlayer.visibility = View.GONE
        serverList = ProxyHelper.serverList(activity)
        val intentFilter = IntentFilter()
        intentFilter.addAction(ProxyHelper.UPDATE_ACTION)
        activity.registerReceiver(this, intentFilter)
    }
    fun hide() {
        playerSource = null
        areaLimitLayout.visibility = View.GONE
        delegate.views.videoPlayer.visibility = View.VISIBLE
        try {
            activity.unregisterReceiver(this)
        } catch (e: Exception) {}
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ProxyHelper.UPDATE_ACTION) {
            serverList = ProxyHelper.serverList(activity)
            if (serverList.isEmpty()) {
                selectedServer = null
            } else {
                proxySpinner.setSelection(0)
                selectedServer = serverList[0]
            }
        }
    }
}