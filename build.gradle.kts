buildscript {

    extra.apply {
        set("compile_sdk_version", 32)
        set("build_tools_version", 32)
        set("target_sdk_version", 32)
        set("kotlin_version", "1.9.22")
    }

    repositories {
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }

    val kotlin_version: String by extra
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.0")
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.aliyun.com/repository/public")
        maven("https://jitpack.io")
    }

}
plugins {
    id("com.google.devtools.ksp") version "1.9.22-1.0.17"
}
tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
