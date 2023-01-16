package com.flydog.connectanya.datalayer.model


import com.google.gson.annotations.SerializedName

data class ReturnClipboardDataModel(
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("data")
    val data: List<ReturnClipboardData>
)