package com.flydog.connectanya.datalayer.model


import com.google.gson.annotations.SerializedName

data class Clipboard(
    @SerializedName("data")
    val data: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("date")
    val date: String
)