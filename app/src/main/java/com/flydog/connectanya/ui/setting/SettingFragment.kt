package com.flydog.connectanya.ui.setting

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.flydog.connectanya.R

class SettingFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
    }
}