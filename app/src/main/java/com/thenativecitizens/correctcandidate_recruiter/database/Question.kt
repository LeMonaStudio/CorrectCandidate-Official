package com.thenativecitizens.correctcandidate_recruiter.database

data class Question(
        var testID: String? = "",
        var questionText: String? = "",
        var answerOptions: MutableList<String> = mutableListOf(),
        var correctOption: String? = "",
        var hasMedia: Boolean = false,
        //MediaType Legend: 0 = No media, 1 = Image, 2 = Pdf, 3 = Audio, 4 = Video
        var mediaType: Int? = 0,
        var mediaUrl: String? = ""
)