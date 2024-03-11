package com.flydog.connectanya.datalayer.model

import android.os.Build
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.google.gson.annotations.SerializedName

data class InputDevice(
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: String,
) {
    companion object {
        fun generateFastObject(): JsonObject {
            val deviceObject = Json.`object`()
            deviceObject.add("type", "Android")
            deviceObject.add("name", Build.MODEL.replaceFirstChar {
                it.uppercaseChar()
            })
            return deviceObject
        }
    }
}
