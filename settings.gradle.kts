pluginManagement {
    includeBuild("bilimiao-build")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/public")
    }
}
rootProject.name = "bilimiao"
include(":app")
include(":miao-binding", ":miao-binding-android")
include(":bilimiao-comm", ":bilimiao-cover", "bilimiao-appwidget", "bilimiao-compose")
include(":DanmakuFlameMaster")
include(":download")
