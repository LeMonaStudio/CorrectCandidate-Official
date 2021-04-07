package com.thenativecitizens.correctcandidate_recruiter.util

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ConnectionTimeOut(application: Application) : AndroidViewModel(application){

    private lateinit var timer: CountDownTimer

    private var _timeOut = MutableLiveData<Int?>()
    val timeOut: LiveData<Int?> get() = _timeOut


    init {
        _timeOut.value = null
    }


    //A countDownTimer for ConnectionTimeOut
    //Begins a 30seconds countdown
    fun beginTimeOutCountDown(duration: Long){
        timer = object : CountDownTimer(duration, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeOut.value = (millisUntilFinished/1_000L).toInt()
            }
            override fun onFinish() {
                //Connection TimeOut
                _timeOut.value = null
            }
        }.start()
    }
    //Cancels the ConnectionTimeOut countdown
    fun cancelTimeOutCountDown(){
        _timeOut.value = null
        timer.cancel()
    }

    //returns the timerOut Status
    fun getIsConnectionTimeOutStat(): Boolean{
        return _timeOut.value == null
    }


    override fun onCleared() {
        super.onCleared()
    }
}