package com.example.fortnightly.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.fortnightly.R

@BindingAdapter("imgUrl")
fun bindImgUrlToImageView(imageView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        Glide.with(imageView.context)
            .load(imgUrl)
            .error(R.drawable.image_placeholder)
            .into(imageView)
    }
}