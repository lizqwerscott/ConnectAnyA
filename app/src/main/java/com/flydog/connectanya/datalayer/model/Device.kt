package com.flydog.connectanya.datalayer.model


import android.os.Build
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.google.gson.annotations.SerializedName

data class Device(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("name")
    val name: String
) {
    companion object {
        fun generateFastObject(deviceId: String): JsonObject {
            val deviceObject = Json.`object`()
            deviceObject.add("id", deviceId)
            deviceObject.add("type", "Android")
            deviceObject.add("name", Build.MODEL.replaceFirstChar {
                it.uppercaseChar()
            })
            return deviceObject
        }
    }
}