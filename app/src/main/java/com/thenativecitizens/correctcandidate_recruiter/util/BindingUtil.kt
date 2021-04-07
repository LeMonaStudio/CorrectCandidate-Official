package com.thenativecitizens.correctcandidate_recruiter.util


import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.thenativecitizens.correctcandidate_recruiter.R

@BindingAdapter("image_uri", "image_Loader")
fun uploadedImage(imageView: ShapeableImageView, imageUri: Uri?, imageLoadingViewModel: ImageLoadingViewModel){
    /*imageUri?.let{
        Glide.with(imageView.context)
                .load(it)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_image_loading)
                        .error(R.drawable.ic_image_loading_error))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                                              isFirstResource: Boolean): Boolean {
                        //Error loading the Image, so let the User know
                        imageLoadingViewModel.onFailedToLoadInGlide()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?,
                                                 dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        return  false
                    }

                })
                .into(imageView)
    }*/
}

@BindingAdapter("sectionNameDisplay")
fun sectionName(textView: MaterialTextView, sectionNm: String?){
    sectionNm?.let {
        textView.text = it
    }
}
