package com.example

import android.app.Application

class CartaQRApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CrashCatcher.install(this)
    }
}
