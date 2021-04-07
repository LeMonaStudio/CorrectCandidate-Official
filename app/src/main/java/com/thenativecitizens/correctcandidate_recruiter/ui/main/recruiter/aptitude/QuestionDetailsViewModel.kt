package com.thenativecitizens.correctcandidate_recruiter.ui.main.recruiter.aptitude

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QuestionDetailsViewModel: ViewModel(){

    private var _sectionList = MutableLiveData<MutableList<String>?>()
    val sectionList: LiveData<MutableList<String>?> get() = _sectionList

    init {
        _sectionList.value = mutableListOf()
    }

    //Add a section to the section list
    fun addSection(sectionName: String){
        val tempList: MutableList<String> = _sectionList.value!!
        tempList.add(sectionName)
        _sectionList.value = tempList
    }

    //Remove a section from the section list
    fun removeSection(index: Int){
        val tempList: MutableList<String> = _sectionList.value!!
        tempList.removeAt(index)
        _sectionList.value = tempList
    }
}