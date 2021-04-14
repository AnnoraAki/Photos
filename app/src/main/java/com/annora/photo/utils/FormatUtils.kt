package com.annora.photo.utils

import java.text.SimpleDateFormat
import java.util.*

val format by lazy(LazyThreadSafetyMode.NONE) {
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
}
val sizeUnit = arrayOf("bytes","KB","MB","GB")

fun formatTime(time: Long): String = format.format(Date(time))

fun formatSize(size:Long) : String {
    var res = size
    var i = 0
    while (res > 1024) {
        res /= 1024
        i += 1
    }
    return "$res${sizeUnit[i]}"
}

fun formatPicSize(width:Int,height:Int) :String {
    return if (width == 0 || height == 0) {
        "不明"
    } else {
        "$width * $height"
    }
}