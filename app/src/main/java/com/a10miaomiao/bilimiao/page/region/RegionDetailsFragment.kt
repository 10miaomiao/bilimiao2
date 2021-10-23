package com.a10miaomiao.bilimiao.page.region

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.page.MainViewModel
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout

class RegionDetailsFragment : Fragment(), DIAware {

    companion object {
        fun newInstance(tid: Int): RegionDetailsFragment {
            val fragment = RegionDetailsFragment()
            val bundle = Bundle()
            bundle.putInt("TID", tid)
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by DI.lazy {
        bindSingleton { ui }
        bindSingleton { this@RegionDetailsFragment }
    }

    private val viewModel by diViewModel<MainViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    val ui = miaoBindingUi {
        verticalLayout {
            views {
                +textView {
                    text = "3434534"
                }
            }
        }
    }
}

