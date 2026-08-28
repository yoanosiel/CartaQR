package com.example

import android.os.Looper
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class StartupTest {
    @Test
    fun laAppArranca() {
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup()
        Shadows.shadowOf(Looper.getMainLooper()).idle()
        println("ACTIVITY CREADA: " + (activity != null))
    }
}
