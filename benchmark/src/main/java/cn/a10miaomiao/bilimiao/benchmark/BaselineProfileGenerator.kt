package cn.a10miaomiao.bilimiao.benchmark

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RequiresApi(Build.VERSION_CODES.P)
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalBaselineProfilesApi::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()


    @Test
    fun generate() {
        rule.collectBaselineProfile(
            packageName = "com.a10miaomiao.bilimiao",
        ) {
            pressHome()
            startActivityAndWait()

        }
    }
}