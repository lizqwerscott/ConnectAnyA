package com.flydog.connectanya.utils

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.UUID

object IdUtils {

    fun generateId(context: Context): String {
        val uuid = UUID.randomUUID().toString()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        with (sharedPreferences.edit()) {
            putString("uuid", uuid)
            apply()
        }
        return uuid
    }

    fun getId(context: Context): String {
        val id = PreferenceManager.getDefaultSharedPreferences(context).getString("uuid", "-1")
        return if (id == "-1") {
            generateId(context)
        } else {
            id ?: ""
        }
    }
}