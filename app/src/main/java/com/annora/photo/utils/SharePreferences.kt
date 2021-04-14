package com.annora.photo.utils

import android.content.Context
import android.content.SharedPreferences
import com.annora.photo.common.DEFAULT_PREFERENCE_FILENAME

val Context.defaultSharedPreferences get() = sharedPreferences(DEFAULT_PREFERENCE_FILENAME)

fun Context.sharedPreferences(name: String): SharedPreferences =
    getSharedPreferences(name, Context.MODE_PRIVATE)

fun SharedPreferences.editor(editorBuilder: SharedPreferences.Editor.() -> Unit) =
    edit().apply(editorBuilder).apply()