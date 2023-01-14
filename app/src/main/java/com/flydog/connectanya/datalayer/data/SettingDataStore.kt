package com.flydog.connectanya.datalayer.data

import androidx.datastore.preferences.preferencesDataStore
import com.flydog.connectanya.App


object SettingDataStore {
    private const val USER_PREFERENCES_NAME = "user_preferences"

    private val App.dataStore by preferencesDataStore(
        name = USER_PREFERENCES_NAME,
    )

    val dataStore = App.instance.dataStore
}