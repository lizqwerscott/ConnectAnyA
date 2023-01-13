package com.flydog.connectanya.ui.setting

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.flydog.connectanya.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportFragmentManager.beginTransaction()
            .replace(R.id.setting_fragment, SettingFragment())
            .commit()
    }
}