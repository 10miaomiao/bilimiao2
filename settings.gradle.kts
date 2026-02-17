pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }
}
rootProject.name = "bilimiao"
include(":app")
include(":bilimiao-comm", ":bilimiao-cover", ":bilimiao-download", "bilimiao-appwidget", "bilimiao-compose")
include(":DanmakuFlameMaster")
include(":benchmark")
include(":grpc-generator")
