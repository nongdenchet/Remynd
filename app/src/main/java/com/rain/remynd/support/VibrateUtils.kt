package com.rain.remynd.support

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

interface VibrateUtils {
    fun execute()
}

class VibrateUtilsImpl(context: Context) : VibrateUtils {
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    override fun execute() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(1000)
        }
    }
}
