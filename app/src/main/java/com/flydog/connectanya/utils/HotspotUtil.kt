package com.flydog.connectanya.utils

import android.content.Context
import android.util.Log

object HotspotUtil {

    fun startHotspot(context: Context) {
        val host = QHotspotEnabler(context)
        host.enableTethering()
        Log.i(TAG, "startHotspot")
    }

    fun stopHotspot(context: Context) {
        val host = QHotspotEnabler(context)
        host.disableTethering()
        Log.i(TAG, "stopHotspot")
    }

    val TAG = "HotspotUtil"
}