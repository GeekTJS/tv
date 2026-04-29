package com.familytv.app.common.extension

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.familytv.app.R

fun ViewGroup.inflate(layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun ImageView.loadImage(url: String?, placeholder: Int = R.drawable.placeholder_image) {
    Glide.with(this.context)
        .load(url)
        .placeholder(placeholder)
        .error(placeholder)
        .centerCrop()
        .into(this)
}
