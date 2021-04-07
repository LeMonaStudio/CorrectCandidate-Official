package com.thenativecitizens.correctcandidate_recruiter.util

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageLoadingViewModel: ViewModel() {

    private var _uri = MutableLiveData<Uri?>()
    val uri: LiveData<Uri?> get() = _uri

    //holds a boolean to know if image loading failed in Glide
    private var _imageLoadFailure = MutableLiveData<Boolean>()
    val imageLoadFailure: LiveData<Boolean> get() = _imageLoadFailure

    init {
        _uri.value = null
        _imageLoadFailure.value = false
    }

    //Set Uri from anywhere
    fun setImageUri(theUri: Uri){
        _uri.value = theUri
    }

    //Set Uri by providing Url string
    fun setImageUriFromUrl(urlString: String){
        _uri.value = Uri.parse(urlString)
    }

    //Called when Image loading fails in BindingAdapter
    fun onFailedToLoadInGlide(){
        _imageLoadFailure.value = true
    }
    //Called after the failure has been handled appropriately
    fun onImageLoadFailureCleared(){
        _imageLoadFailure.value = false
    }
}