package com.flydog.connectanya.datalayer.repository

import android.util.Log
import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.flydog.connectanya.datalayer.model.RegisterModel
import com.flydog.connectanya.utils.HttpUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

sealed class LoginResult<out R> {
    data class Success<out T>(val data: T) : LoginResult<T>()
    data class Error(val exception: Exception) : LoginResult<Nothing>()
}

class LoginRepository {

    suspend fun makeRegisterUserRequest(ip: String, username: String, deviceId: String): LoginResult<RegisterModel> {
        return withContext(Dispatchers.IO) {
            val url = "https://$ip/user/adduser"

            val deviceObject = Json.`object`()
            deviceObject.add("id", deviceId)
            deviceObject.add("type", "Android")

            val data: JsonObject = Json.`object`().add("device", deviceObject).add("name", username)

            // send
            val str = try {
                HttpUtils.httpPost(url, data.toString(), 30000).toString()
            } catch (e: Exception) {
                Log.e(TAG, "$e")
                ""
            }
            Log.i(TAG, "ip: $url, result:$str")
            if (str == "" || str == "Internal Server Error") {
                Log.e(TAG, "Internal Server Error or result is null")
                LoginResult.Error(Exception("Internal Server Error or result is null"))
            } else {
                val gson = Gson()
                LoginResult.Success(gson.fromJson(str, RegisterModel::class.java))
            }
        }
    }
    
    companion object {
        const val TAG = "LoginRepository"
    }
}