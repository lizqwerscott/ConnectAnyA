package com.flydog.connectanya.datalayer.repository

import android.util.Log
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.flydog.connectanya.datalayer.model.Device
import com.flydog.connectanya.utils.HttpUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class LoginResult<out R> {
    data class Success<out T>(val data: T) : LoginResult<T>()
    data class Error(val exception: Exception) : LoginResult<Nothing>()
}

class LoginRepository {

    suspend fun makeRegisterUserRequest(ip: String, username: String, deviceId: String): LoginResult<Boolean> {
        return withContext(Dispatchers.IO) {
            val url = "http://$ip:8686/user/adduser"

            val deviceObject = Device.generateFastObject(deviceId)

            val data: JsonObject = Json.`object`().add("device", deviceObject).add("name", username)

            // send
            val str = try {
                HttpUtils.httpPost(url, data.toString(), 30000).toString()
            } catch (e: Exception) {
                Log.e(TAG, "$e")
                ""
            }
            Log.i(TAG, "ip: $url, result:$str")
            val res = HttpUtils.handleReturnJson(str)
            if (res == "-1") {
                Log.e(TAG, "Internal Server Error or result is null")
                LoginResult.Error(Exception("Internal Server Error or result is null"))
            } else {
                val boolean = Json.parse(res)
                if (boolean.isBoolean) {
                    LoginResult.Success(boolean.asBoolean())
                } else {
                    LoginResult.Success(false)
                }
            }
        }
    }
    
    companion object {
        const val TAG = "LoginRepository"
    }
}