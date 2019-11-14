package com.rain.remynd.ui

import android.util.Log

fun execute(times: Int = 3, delay: Long = 1000, action: () -> Unit) {
    var current = 0
    while (true) {
       try {
           action()
           break
       } catch (e: Throwable) {
           if (current >= times) throw e
           else current++

           Log.e("execute","Retrying action: times=$current, delay=$delay", e)
           Thread.sleep(delay)
       }
    }
}
