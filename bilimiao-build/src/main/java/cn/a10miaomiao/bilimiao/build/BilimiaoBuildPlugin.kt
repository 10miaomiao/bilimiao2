package cn.a10miaomiao.bilimiao.build

import org.gradle.api.Plugin
import org.gradle.api.Project

class BilimiaoBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("BilimiaoBuildPlugin")
    }
}