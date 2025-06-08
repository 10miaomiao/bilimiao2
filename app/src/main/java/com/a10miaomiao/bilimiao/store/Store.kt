package com.a10miaomiao.bilimiao.store

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.store.AppStore
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.MessageStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.RegionStore
import com.a10miaomiao.bilimiao.comm.store.TimeSettingStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton

class Store (
        private val activity: AppCompatActivity,
        override val di: DI,
): DIAware {

        val appStore: AppStore by activity.diViewModel(di)
        val windowStore: WindowStore by activity.diViewModel(di)
        val playListStore: PlayListStore by activity.diViewModel(di)
        val playerStore: PlayerStore by activity.diViewModel(di)
        val userStore: UserStore by activity.diViewModel(di)
        val messageStore: MessageStore by activity.diViewModel(di)
        val timeSettingStore: TimeSettingStore by activity.diViewModel(di)
        val filterStore: FilterStore by activity.diViewModel(di)
        val regionStore: RegionStore by activity.diViewModel(di)

        fun loadStoreModules(diBuilder: DI.Builder) = diBuilder.run{
                bindSingleton { appStore }
                bindSingleton { windowStore }
                bindSingleton { playListStore }
                bindSingleton { playerStore }
                bindSingleton { userStore }
                bindSingleton { messageStore }
                bindSingleton { timeSettingStore }
                bindSingleton { filterStore }
                bindSingleton { regionStore }
        }

        fun onCreate(savedInstanceState: Bundle?) {
                appStore.init(activity)
                windowStore.init(activity)
                playerStore.init(activity)
                userStore.init(activity)
                messageStore.init(activity)
                timeSettingStore.init(activity)
                filterStore.init(activity)
                regionStore.init(activity)
        }

        fun onDestroy() {

        }

}