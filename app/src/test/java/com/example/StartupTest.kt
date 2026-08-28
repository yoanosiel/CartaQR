package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.repository.MenuRepository
import com.example.ui.navigation.CartaQRNavGraph
import com.example.ui.theme.CartaQRTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class StartupTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun laAppArranca() {
        val context = RuntimeEnvironment.getApplication()
        compose.setContent {
            CartaQRTheme {
                CartaQRNavGraph(repository = MenuRepository(context))
            }
        }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()
    }
}
