package com.flydog.connectanya.utils

import android.util.Log
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.flydog.connectanya.datalayer.model.*
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

    private fun isError(str: String): Boolean {
        var isJsonStr = true
        try {
            Json.parse(str)
        } catch (e: Exception) {
            isJsonStr = false
        }
        return str == "" || !isJsonStr || str == "Internal Server Error" || str.contains("error") || str.contains("ERROR") || str.contains("Error")
    }

    fun handleReturnJson(str: String): String {
        var result = "-1"

        if (isError(str)) {
            return result
        }

        val res = Json.parse(str) as JsonObject
        val code = res.getInt("code", -1)
        val msg = res.getString("msg", "-1")

        if (code != -1 && msg != "-1") {
            if (code == 200) {
                Log.i(TAG, msg)
                val data = res.get("data")
                result = data.toString()
            } else {
                Log.e(TAG, "code: $code, msg: $msg")
            }
        }
        return result
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

    suspend fun addMessage(ip: String, message: String, deviceId: String): Boolean {
        val url = "http://$ip/message/addmessage"

        Log.i(TAG, "message: $message, deviceId: $deviceId")

        val deviceObject = Device.generateFastObject(deviceId)

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
        val res = handleReturnJson(str)
        return if (res == "-1") {
            Log.e(TAG, "Internal Server Error or result is null")
            false
        } else {
            val boolean = Json.parse(res)
            if (boolean.isBoolean) {
                boolean.asBoolean()
            } else {
                false
            }
        }
    }

    suspend fun updateMessage(ip: String, deviceId: String): List<ReturnClipboardData>? {
        val url = "http://$ip/message/update"

        val deviceObject = Device.generateFastObject(deviceId)

        val data: JsonObject = Json.`object`().add("device", deviceObject)

        // send
        val str = try {
            httpPost(url, data.toString(), 30000).toString()
        } catch (e: Exception) {
            Log.e(TAG, "$e")
            ""
        }

        Log.i(TAG, "ip: $url, result:$str")
        return if (isError(str)) {
            Log.e(TAG, "Internal Server Error or result is null")
            null
        } else {
            val gson = Gson()
            val model = gson.fromJson(str, ReturnClipboardDataModel::class.java)
            if (model.code == 200) {
                Log.i(TAG, model.msg)
                model.data
            } else {
                Log.i(TAG, "code: ${model.code}, msg: ${model.msg}")
                null
            }
        }
    }

    suspend fun updateBaseMessage(ip: String, deviceId: String): Clipboard? {
        val url = "http://$ip/message/updatebase"

        val deviceObject = Device.generateFastObject(deviceId)

        val data: JsonObject = Json.`object`().add("device", deviceObject)

        // send
        val str = try {
            httpPost(url, data.toString(), 30000).toString()
        } catch (e: Exception) {
            Log.e(TAG, "$e")
            ""
        }

        Log.i(TAG, "ip: $url, result:$str")
        val res = handleReturnJson(str)
        return if (res == "-1") {
            Log.e(TAG, "Internal Server Error or result is null")
            null
        } else {
            val gson = Gson()
            val clipboard = gson.fromJson(res, Clipboard::class.java)
            clipboard
        }
    }
}