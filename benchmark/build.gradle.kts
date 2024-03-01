plugins {
    id("com.android.test")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "cn.a10miaomiao.bilimiao.benchmark"
    compileSdk = 33

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    defaultConfig {
        minSdk = 23
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

//        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR,DEBUGGABLE"
    }

    buildTypes {
        // This benchmark buildType is used for benchmarking, and should function like your
        // release build (for example, with minification on). It"s signed with a debug key
        // for easy local/CI testing.
        create("benchmark") {
            isDebuggable = true
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
        }
    }

    flavorDimensions += listOf("default")
    productFlavors {
        create("dev") { dimension = "default" }
        create("github") { dimension = "default" }
        create("gitee") { dimension = "default" }
        create("qq") { dimension = "default" }
        create("miao") { dimension = "default" }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation("androidx.test.ext:junit:1.1.5")
    implementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.test.uiautomator:uiautomator:2.2.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.1.1")
}

androidComponents {
    beforeVariants(selector().all()) {
        it.enable = it.buildType == "benchmark"
    }
}