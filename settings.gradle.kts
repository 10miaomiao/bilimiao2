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
        // 不要引入 aliyun 镜像：同步延迟 / 缓存不一致会导致依赖解析到错误版本或直接失败
        mavenLocal()
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://gitlab.com/api/v4/projects/38224197/packages/maven")
    }
}
rootProject.name = "bilimiao"
include(":app")
include(":desktop-app")
include(":bilimiao-comm", ":bilimiao-cover", ":bilimiao-download", "bilimiao-appwidget", "bilimiao-compose")
include(":bilimiao-grpc")
include(":bilimiao-grpc:proto")
include(":bilimiao-grpc:generator")
// include(":DanmakuFlameMaster") // 已移除：统一使用 KMP danmaku-engine
include(":danmaku-engine")
include(":benchmark")
