
plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

tasks.withType<JavaCompile>().all {
    enabled = false
}

dependencies {
    compileOnly(libs.pbandk.runtime)
    compileOnly(libs.pbandk.genlib)
}