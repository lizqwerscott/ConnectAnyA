package com.flydog.connectanya.datalayer.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flydog.connectanya.datalayer.data.SettingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.*

data class UserData(
    val username: String,
    val deviceId: String,
)

class UserDataRepository(private val dataStore: DataStore<Preferences> = SettingDataStore.dataStore) {

    private val TAG: String = "UserDataRepo"

    private object PreferencesKeys {
        val USER_NAME = stringPreferencesKey("user_name")
        val DEVICE_ID = stringPreferencesKey("device_id")
    }

    val userDataFlow: Flow<UserData> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences.", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { data ->
            mapUserData(data)
        }

    suspend fun updateUserName(username: String) {
        dataStore.edit { data ->
            data[PreferencesKeys.USER_NAME] = username
        }
    }

    suspend fun fetchInitialData() = mapUserData(dataStore.data.first().toPreferences())

    private fun mapUserData(preferences: Preferences): UserData {
        val username = preferences[PreferencesKeys.USER_NAME] ?: ""

        val uuid = UUID.randomUUID().toString()
        val deviceId = preferences[PreferencesKeys.DEVICE_ID] ?: uuid
        return UserData(username, deviceId)
    }
}