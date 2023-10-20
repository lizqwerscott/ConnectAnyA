package com.flydog.connectanya.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    // TODO: 变成Repository 像任务一样实时更新
    // 现在只返回基础信息
    val currentClipboardData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setClipboardData(item: String) {
        currentClipboardData.value = item
    }
}