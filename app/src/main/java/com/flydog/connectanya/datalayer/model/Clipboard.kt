package com.flydog.connectanya.datalayer.model


import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.google.gson.annotations.SerializedName

data class Clipboard(
    @SerializedName("data")
    val data: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("date")
    val date: Long
) {
    companion object {
        fun generateFastObject(message: String): JsonObject {
            val timestamp = System.currentTimeMillis()
            val clipboardObject = Json.`object`()
            clipboardObject.add("type", "Text")
            clipboardObject.add("data", message)
            clipboardObject.add("date", timestamp)
            return clipboardObject
        }
    }
}
