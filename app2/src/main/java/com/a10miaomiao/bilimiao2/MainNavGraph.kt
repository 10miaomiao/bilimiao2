package com.a10miaomiao.bilimiao2

import androidx.navigation.NavController
import androidx.navigation.createGraph
import androidx.navigation.fragment.fragment
import com.a10miaomiao.bilimiao2.page.MainFragment

object MainNavGraph {
    // Counter for id's. First ID will be 1.
    var id_counter = 1
    val id = id_counter++

    object dest {
        val main = id_counter++
        val home = id_counter++
    }

    object action {
        val to_plant_detail = id_counter++
        val blank_to_main = id_counter++
    }

    object args {
        const val plant_id = "plantId"
    }

    fun createGraph (navController: NavController) {
        navController.graph = navController.createGraph(id, dest.home) {
            fragment<MainFragment>(dest.home)

        }
    }


}