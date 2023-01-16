package com.flydog.connectanya.datalayer.model


import com.flydog.connectanya.datalayer.data.ClipboardData
import com.google.gson.annotations.SerializedName

data class ReturnClipboardData(
    @SerializedName("clipboards")
    val clipboards: List<Clipboard>,
    @SerializedName("device")
    val device: Device
) {
    fun convertClipboardData(): List<ClipboardData> {
        return clipboards.map {
            ClipboardData(device, it)
        }
    }
}