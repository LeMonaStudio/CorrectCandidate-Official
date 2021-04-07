package com.thenativecitizens.correctcandidate_recruiter.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*

class SplashViewModel : ViewModel() {
    //Coroutine
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)


    private var _onSplashCompleted = MutableLiveData<Boolean>()
    val onSplashCompleted: LiveData<Boolean> get() = _onSplashCompleted


    init {
        _onSplashCompleted.value = false
        delaySplash()
    }

    private fun delaySplash() {
        uiScope.launch {
            delay(3000).let {
                _onSplashCompleted.value = true
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}