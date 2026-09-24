// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    extra.apply {
        set("compile_sdk_version", 37)
        set("build_tools_version", 37)
        set("target_sdk_version", 37)
    }

    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }

}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.jetbrains.kotlin.parcelize) apply false
    alias(libs.plugins.google.protobuf)  apply false
    alias(libs.plugins.ksp) apply false
}

// === 应用版本号唯一来源：gradle.properties ===
// CI 可用 -Pbilimiao.versionName=2.5.2 -Pbilimiao.versionSuffix= 覆盖（见 .github/workflows/）。
// app（Android）与 desktop-app 都从这里取值，避免两处硬编码各改各的。
val bilimiaoBaseVersion = providers.gradleProperty("bilimiao.versionName").get()
val bilimiaoVersionSuffix = providers.gradleProperty("bilimiao.versionSuffix").getOrElse("")

// Windows Installer 的 ProductVersion 只接受 MAJOR.MINOR.BUILD 三段，jpackage/Compose
// 会在配置阶段直接报错，而 Android 侧历史上出现过 4 段版本（如 2.4.8.1），所以统一截断/补齐到三段。
// 副作用：2.4.8 与 2.4.8.1 会得到相同的 MSI 版本号，Windows 无法区分这两次安装。
val bilimiaoPackageVersion = bilimiaoBaseVersion.split(".").let { parts ->
    if (parts.size >= 3) parts.take(3).joinToString(".")
    else (parts + List(3 - parts.size) { "0" }).joinToString(".")
}

extra.apply {
    // 展示版本："2.5.1" 或 "2.5.1 beta"
    set(
        "bilimiaoVersionName",
        if (bilimiaoVersionSuffix.isBlank()) bilimiaoBaseVersion else "$bilimiaoBaseVersion $bilimiaoVersionSuffix"
    )
    set("bilimiaoPackageVersion", bilimiaoPackageVersion)
    set("bilimiaoVersionCode", providers.gradleProperty("bilimiao.versionCode").get().toInt())
}

allprojects {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            // This lib is used by com.github.mikaelzero.mojito:SketchImageViewLoader:1.8.7 and only available in bintray
            // It has been moved to mavenCentral with a different module name
            substitute(module("me.panpf:sketch-gif:2.7.1")).using(module("io.github.panpf.sketch:sketch-gif:2.7.1"))
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
