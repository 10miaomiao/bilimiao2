pluginManagement {
    includeBuild("bilimiao-build")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") //pbandk
    }
}
rootProject.name = "bilimiao"
include(":app")
include(":miao-binding", ":miao-binding-android")
include(":bilimiao-comm", ":bilimiao-cover", ":bilimiao-download", "bilimiao-appwidget", "bilimiao-compose")
include(":DanmakuFlameMaster")
include(":benchmark")
include(":grpc-generator")
