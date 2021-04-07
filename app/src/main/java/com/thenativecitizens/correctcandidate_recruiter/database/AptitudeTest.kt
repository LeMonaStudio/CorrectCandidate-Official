package com.thenativecitizens.correctcandidate_recruiter.database

data class AptitudeTest(
        var testID: String? = "",
        var testTitle: String? = "",
        var sections: MutableList<Section>? = mutableListOf(),
        var testDuration: Int? = 0,
        var testDescription: String? = "",
        var bgCoverArtUrl: String? = "",
        var isByInvitation: Boolean = false,
        var invitedCandidates: MutableList<String>? = mutableListOf()
)
