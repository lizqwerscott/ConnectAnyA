package com.flydog.connectanya.utils

import android.content.ClipboardManager
import android.content.Context


object ClipboardUtil {
    fun getClipboardTextFront(context: Context): String {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClip!!.itemCount > 0) {
            return clipboard.primaryClip?.getItemAt(0)?.text.toString()
        }
        return ""
    }

}