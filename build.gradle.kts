// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    extra.apply {
        set("compile_sdk_version", 32)
        set("build_tools_version", 32)
        set("target_sdk_version", 32)
    }

    repositories {
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }

}

plugins {
    val kotlinVersion = "2.0.20"
    id("com.android.application") version "8.5.1" apply false
    id("com.android.library") version "8.5.1" apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion apply false
    id("com.google.protobuf") version "0.9.4" apply false
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
