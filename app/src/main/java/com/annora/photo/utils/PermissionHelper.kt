package com.annora.photo.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

object PermissionHelper {

    const val PERMISSION_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE
    const val PERMISSION_WRITE_CODE = 111

    fun checkWritePermission(context: Context) = ContextCompat.checkSelfPermission(
        context,
        PERMISSION_WRITE
    )

    fun openAppSettings(activity: AppCompatActivity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        activity.startActivity(intent)
    }


}