package com.ext.kernelmanager.core.crash

import android.content.Context
import android.content.Intent
import android.util.Log
import com.ext.kernelmanager.MainActivity
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class GlobalCrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Save crash info to local file
            val crashFile = File(context.filesDir, "last_crash.log")
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            crashFile.writeText(sw.toString())

            // Mark a flag that app crashed
            val prefs = context.getSharedPreferences("crash_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("did_crash", true).commit()

            Log.e("GlobalCrashHandler", "Fatal crash caught. Log saved.", throwable)

            // Terminate process immediately
            exitProcess(1)
        } catch (e: Exception) {
            // Fallback to default
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
