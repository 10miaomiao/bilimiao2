package com.a10miaomiao.bilimiao.template

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.a10miaomiao.bilimiao.comm.*
import org.kodein.di.*
import splitties.views.dsl.core.*

class TemplateFragment : Fragment(), DIAware {

    override val di: DI by DI.lazy {
        bindSingleton { ui }
        bindSingleton { this@TemplateFragment }
    }

    private val viewModel by diViewModel<TemplateViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    val ui = miaoBindingUi {
        verticalLayout {

        }
    }

}