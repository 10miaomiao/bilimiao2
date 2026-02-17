// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    extra.apply {
        set("compile_sdk_version", 36)
        set("build_tools_version", 36)
        set("target_sdk_version", 36)
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
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.serialization) apply false
    alias(libs.plugins.google.protobuf)  apply false
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
