
plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

// 固定 jar 名，供 :bilimiao-grpc:proto 的 pbandk 生成任务（kotlin_service_gen option）引用
tasks.jar {
    archiveBaseName.set("grpc-generator")
}

tasks.withType<JavaCompile>().all {
    enabled = false
}

dependencies {
    compileOnly(libs.pbandk.runtime)
    compileOnly(libs.pbandk.genlib)
}