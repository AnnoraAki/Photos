package com.annora.photo.utils

import android.os.Environment
import android.util.Base64
import com.annora.photo.common.OWN_FILE_NAME
import java.io.File


/**
 * 将名字加密换为唯一的相册路径
 */
fun encodeForPath(name: String): String = Base64.encodeToString(name.toByteArray(), Base64.DEFAULT)

fun getTimeStamp() = System.currentTimeMillis()

fun getOriginPath() = Environment.getExternalStorageDirectory().absolutePath + File.separator + OWN_FILE_NAME