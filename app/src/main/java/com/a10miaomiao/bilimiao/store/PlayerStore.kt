package com.a10miaomiao.bilimiao.store

import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.download.LocalPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.model.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.model.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI

class PlayerStore(override val di: DI) :
    ViewModel(), BaseStore<PlayerStore.State> {

    companion object {
        const val VIDEO = "video"
        const val BANGUMI = "bangumi"
    }

    data class State (
        var type: String = "",
        var aid: String = "",
        var cid: String = "",
        var epid: String = "",
        var sid: String = "",
        var title: String = "",
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    fun setPlayerSource(source: BasePlayerSource) {
        when (source) {
            // 普通视频
            is VideoPlayerSource -> {
                this.setState {
                    this.type = VIDEO
                    this.aid = source.aid
                    this.cid = source.id
                    this.title = source.title
                    this.sid = ""
                    this.epid = ""
                }
            }
            // 番剧
            is BangumiPlayerSource -> {
                this.setState {
                    this.type = BANGUMI
                    this.aid = ""
                    this.cid = source.id
                    this.title = source.title
                    this.sid = source.sid
                    this.epid = source.epid
                }
            }
            // 本地视频
            is LocalPlayerSource -> {
                this.setState {
                    this.type = VIDEO
                    this.aid = source.ownerId
                    this.cid = source.id
                    this.title = source.title
                    this.sid = ""
                    this.epid = ""
                }
            }
        }
    }


    fun clearPlayerInfo() {
        this.setState {
            this.type = ""
            this.aid = ""
            this.cid = ""
            this.title = ""
            this.sid = ""
            this.epid = ""
        }
    }

}