package com.flydog.connectanya.utils

import android.util.Log
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.flydog.connectanya.datalayer.model.RegisterModel
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString.Companion.encodeUtf8
import java.lang.Exception
import java.util.concurrent.TimeUnit

object HttpUtils {

    private const val TAG = "HttpUtils"
    private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    fun httpGet(url: String, timeout: Long = 1000): String? {
        val client = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .build()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    fun httpPost(url: String, data: String, timeout: Long = 1000): String? {
        val client = OkHttpClient.Builder()
            .connectTimeout(timeout, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(data.encodeUtf8().toRequestBody(MEDIA_TYPE_JSON))
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

}