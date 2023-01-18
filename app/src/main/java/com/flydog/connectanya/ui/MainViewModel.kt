package com.flydog.connectanya.ui

import androidx.lifecycle.*
import com.flydog.connectanya.datalayer.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.liveData
import com.flydog.connectanya.datalayer.model.ReturnBoolDataModel
import com.flydog.connectanya.datalayer.repository.LoginRepository
import com.flydog.connectanya.datalayer.repository.LoginResult
import java.lang.Exception

class MainViewModel(
    private val userDataRepository: UserDataRepository = UserDataRepository(),
    private val loginRepository: LoginRepository = LoginRepository()
) : ViewModel() {

    val initialSetupEvent = liveData {
        emit(userDataRepository.fetchInitialData())
    }

    val userDataUiModel = userDataRepository.userDataFlow.asLiveData()

    fun updateUserName(username: String) {
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepository.updateUserName(username)
        }
    }

    fun updateDeviceId() {
        viewModelScope.launch(Dispatchers.IO) {
            userDataRepository.updateDeviceId()
        }
    }

    // TODO: 变成Repository 像任务一样实时更新
    // 现在只返回基础信息
    val currentClipboardData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setClipboardData(item: String) {
        currentClipboardData.value = item
    }

    suspend fun login(ip: String, username: String): LoginResult<ReturnBoolDataModel> {
        userDataUiModel.value?.deviceId?.let {
            return loginRepository.makeRegisterUserRequest(ip, username, it)
        }
        return LoginResult.Error(Exception("device not found"))
    }
}

//class MainViewModelFactory(
//    private val userDataRepository: UserDataRepository
//) : ViewModelProvider.Factory {
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return MainViewModel(userDataRepository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
