package com.thenativecitizens.correctcandidate_recruiter.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.thenativecitizens.correctcandidate_recruiter.util.UserType

class UserTypeViewModel: ViewModel() {

    private var _userTypeSelected = MutableLiveData<UserType>()
    val userTypeSelected: LiveData<UserType> get() = _userTypeSelected

    init {
        _userTypeSelected.value = UserType.UNDECIDED
    }

    //Update UserTypeSelected
    fun updateUserType(userType: UserType){
        _userTypeSelected.value = userType
    }
}