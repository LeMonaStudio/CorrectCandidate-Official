package com.thenativecitizens.correctcandidate_recruiter.database

data class Section(
        var testID: String? = "",
        var sectionName: String? = "",
        var questionList: MutableList<Question>? = mutableListOf()
)