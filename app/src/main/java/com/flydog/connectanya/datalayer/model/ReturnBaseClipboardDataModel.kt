package com.flydog.connectanya.datalayer.model


import com.google.gson.annotations.SerializedName

data class ReturnBaseClipboardDataModel(
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("data")
    val data: Clipboard
)