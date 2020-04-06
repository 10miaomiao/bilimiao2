package com.a10miaomiao.bilimiao

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Base64
import android.util.Log
import com.a10miaomiao.bilimiao.utils.AESUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        val filesDir = appContext.filesDir
        println(filesDir)
        assertEquals("com.a10miaomiao.bilimiao", appContext.packageName)
    }

}
