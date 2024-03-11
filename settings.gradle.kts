pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

rootProject.name = "bilimiao"
include(":app")
include(":bilimiao-comm", ":bilimiao-download")

