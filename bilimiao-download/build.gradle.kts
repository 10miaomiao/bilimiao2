import cn.a10miaomiao.bilimiao.build.*

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("bilimiao-build")
    kotlin("plugin.serialization")
}

android {
    namespace = "cn.a10miaomiao.bilimiao.download"
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(Libraries.core)
    implementation(Libraries.appcompat)
    implementation(Libraries.material)

    implementation(Libraries.kotlinxSerializationJson)
    implementation(Libraries.okhttp3)

    implementation(project(":bilimiao-comm"))
    implementation(project(":DanmakuFlameMaster"))

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.androidxJunit)
    androidTestImplementation(Libraries.espresso)
}