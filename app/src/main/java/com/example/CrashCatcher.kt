package com.example

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object CrashCatcher {
    fun install(context: Context) {
        val app = context.applicationContext
        val default = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val log = "Thread: " + thread.name + "\n" + sw.toString()
                app.getExternalFilesDir(null)?.let { File(it, "crash.txt").writeText(log) }
                try { File("/storage/emulated/0/Download/crash_cartaqr.txt").writeText(log) } catch (_: Exception) {}
            } catch (_: Exception) {}
            default?.uncaughtException(thread, throwable)
        }
    }
}
