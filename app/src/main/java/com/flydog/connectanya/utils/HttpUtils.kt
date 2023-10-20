package com.flydog.connectanya.utils

import android.util.Log
import com.eclipsesource.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.format
import okio.ByteString.Companion.encodeUtf8
import java.util.concurrent.TimeUnit

object HttpUtils {

    private const val TAG = "HttpUtils"
    private val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()

    private fun isError(str: String): Boolean {
        var isJsonStr = true
        try {
            Json.parse(str)
        } catch (e: Exception) {
            isJsonStr = false
        }
        return str == "" || !isJsonStr || str == "Internal Server Error" || str.contains("error") || str.contains("ERROR") || str.contains("Error")
    }

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

    fun sendBarkMessage(barkKey: String, clipboardMessage: String) : Boolean {
        val res = httpGet(format("https://api.day.app/${barkKey}/clipboard/${clipboardMessage}"), 1000)
        Log.w("bark", "Res: $res")
        return true
    }
}