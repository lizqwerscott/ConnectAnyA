package com.flydog.connectanya.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val currentClipboardData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setClipboardData(item: String) {
        currentClipboardData.value = item
    }
}