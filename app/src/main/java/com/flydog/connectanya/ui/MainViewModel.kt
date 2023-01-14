package com.flydog.connectanya.ui

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import com.flydog.connectanya.datalayer.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.liveData
import com.flydog.connectanya.datalayer.model.RegisterModel
import com.flydog.connectanya.datalayer.repository.LoginRepository
import com.flydog.connectanya.datalayer.repository.LoginResult
import java.lang.Exception

data class UserDataUiModel(
    val username: String,
    val deviceId: String
)

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

    val currentClipboardData: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun setClipboardData(item: String) {
        currentClipboardData.value = item
    }

    suspend fun login(ip: String, username: String): LoginResult<RegisterModel> {
        userDataUiModel.value?.deviceId?.let {
            val res = loginRepository.makeRegisterUserRequest(ip, username, it)
            return res
        }
        return LoginResult.Error(Exception("device not found"))

//            if (model != null) {
//                if (model.code == 200) {
//                    userDataRepository.updateUserName(username)
////                    Toast.makeText(context, "提交完成", Toast.LENGTH_SHORT).show()
//                } else {
////                    Toast.makeText(context, model.msg, Toast.LENGTH_SHORT).show()
//                }
//            } else {
////                Toast.makeText(context, "网络连接错误，请检查网络或者检查默认服务器地址", Toast.LENGTH_SHORT).show()
//            }
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
