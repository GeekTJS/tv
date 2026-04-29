package com.familytv.app.common.extension

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("app:formattedTime")
fun TextView.setFormattedTime(milliseconds: Long) {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    text = if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
