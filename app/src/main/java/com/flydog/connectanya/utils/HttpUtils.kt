package com.flydog.connectanya.utils

import android.util.Log
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.flydog.connectanya.datalayer.model.ReturnBoolDataModel
import com.flydog.connectanya.datalayer.repository.LoginResult
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

    suspend fun addMessage(ip: String, message: String, deviceId: String): Boolean {
        val url = "https://$ip/message/addmessage"

        val deviceObject = Json.`object`()
        deviceObject.add("id", deviceId)
        deviceObject.add("type", "Android")

        val messageObject = Json.`object`().add("data", message).add("type", "text")

        val data: JsonObject = Json.`object`().add("device", deviceObject).add("message", messageObject)

        // send
        val str = try {
            httpPost(url, data.toString(), 30000).toString()
        } catch (e: Exception) {
            Log.e(TAG, "$e")
            ""
        }

        Log.i(TAG, "ip: $url, result:$str")
        return if (str == "" || str == "Internal Server Error") {
            Log.e(TAG, "Internal Server Error or result is null")
            false
        } else {
            val gson = Gson()
            val model = gson.fromJson(str, ReturnBoolDataModel::class.java)
            if (model.code == 200) {
                Log.i(TAG, model.msg)
                true
            } else {
                Log.i(TAG, "code: ${model.code}, msg: ${model.msg}")
                false
            }
        }
    }

}