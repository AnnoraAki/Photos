package com.annora.photo.utils

import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

var screenHeight = 0
var screenWeight = 0

fun Context.getScreenHeight(): Int {
    if (screenHeight == 0) {
        val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenHeight = size.y
    }
    return screenHeight
}

fun Context.getScreenWeight(): Int {
    if (screenWeight == 0) {
        val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWeight = size.x
    }
    return screenWeight
}

fun Context.toast(str: String) {
    Toast.makeText(this, str, Toast.LENGTH_SHORT).show()
}


fun AppCompatActivity.setBackgroundAlpha(bgAlpha:Float) {
    val lp = this.window.attributes
    lp.alpha = bgAlpha;
    if (bgAlpha == 1f) {
        this.window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    } else {
        this.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
    this.window.attributes = lp
}