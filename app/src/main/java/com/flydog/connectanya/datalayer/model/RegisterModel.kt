package com.flydog.connectanya.datalayer.model

import com.google.gson.annotations.SerializedName

data class RegisterModel(
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("data")
    val data: String
)