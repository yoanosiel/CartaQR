package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.CartaQRNavGraph
import com.example.ui.theme.CartaQRTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
        CrashCatcher.install(this)
    enableEdgeToEdge()
    setContent {
      CartaQRTheme {
        CartaQRNavGraph()
      }
    }
  }
}
