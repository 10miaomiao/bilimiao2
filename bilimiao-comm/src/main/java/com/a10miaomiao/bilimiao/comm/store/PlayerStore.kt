package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.delegate.player.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI


class PlayerStore(override val di: DI) :
    ViewModel(), BaseStore<PlayerStore.State> {

    companion object {
        const val VIDEO = "video"
        const val BANGUMI = "bangumi"
    }

    data class State(
        var type: String = "",
        var aid: String = "",
        var cid: String = "",
        var epid: String = "",
        var sid: String = "",
        var title: String = "",
        var cover: String = "",
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    fun setPlayerSource(source: BasePlayerSource) {
        val ids = source.getSourceIds()
        this.setState {
            cid = source.id
            title = source.title
            cover = source.coverUrl
            if (ids.sid.isNotBlank() && ids.epid.isNotBlank()) {
                type = BANGUMI
                sid = ids.sid
                epid = ids.epid
            } else {
                type = VIDEO
                aid = ids.aid
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

