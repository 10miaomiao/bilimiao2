// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    extra.apply {
        set("compile_sdk_version", 32)
        set("build_tools_version", 32)
        set("target_sdk_version", 32)

        set("material_version", "1.6.1")
        set("splitties_version", "3.0.0-beta06")
        set("nav_version", "2.3.5")
        set("kodein_di_version", "7.12.0")
        set("lifecycle_version", "2.5.1")
        set("compose_version", "1.2.1")

        set("okhttp_version", "4.10.0")
        set("gson_version", "2.10.1")
        set("mojito_version", "1.8.7")
    }

    repositories {
        google()
        mavenCentral()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://maven.aliyun.com/repository/public")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
