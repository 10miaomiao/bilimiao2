import cn.a10miaomiao.bilimiao.build.*

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("bilimiao-build")
}

android {
    compileSdk = 30

    defaultConfig {
        minSdk = 21
        targetSdk = 33
        version = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("release") {
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
    namespace = "cn.a10miaomiao.bilimiao.cover"
}

dependencies {
    implementation(Libraries.core)
    implementation(Libraries.appcompat)
    implementation(Libraries.material)
    implementation(Libraries.lifecycleViewModel)

    implementation(Libraries.kotlinxCoroutinesAndroid)
    implementation(Libraries.glide)
    implementation(Libraries.pbandkRuntime)
    implementation(Libraries.okhttp3)

    // 图片预览工具
    implementationMojito()

    implementation(project(":bilimiao-comm"))

    testImplementation(Libraries.junit)
    androidTestImplementation(Libraries.androidxJunit)
    androidTestImplementation(Libraries.espresso)

}