package com.a10miaomiao.bilimiao.store

import androidx.core.graphics.Insets
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI

class PlayerStore(override val di: DI) :
    ViewModel(), BaseStore<PlayerStore.State> {

    data class State (
        var info: PlayerSourceInfo = PlayerSourceInfo()
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    fun setPlayerInfo(info: PlayerSourceInfo) {
        this.setState {
            this.info = info
        }
    }


    fun clearPlayerInfo() {
        this.setState {
            this.info = PlayerSourceInfo()
        }
    }

}