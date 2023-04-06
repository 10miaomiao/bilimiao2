plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins.register("bilimiao-build") {
        id = "bilimiao-build"
        implementationClass = "cn.a10miaomiao.bilimiao.build.BilimiaoBuildPlugin"
    }
}