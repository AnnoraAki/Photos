package com.annora.photo.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.yalantis.ucrop.util.FileUtils.getDataColumn
import java.io.*


object Uri2FileRealPathUtils {
    fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // 1. DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // 1.1 ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory()
                        .absolutePath + File.separator + split[1]
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(
                    context,
                    contentUri, null, null
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs =
                    arrayOf(split[1])
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return when {
                isGooglePhotosUri(uri) -> { //判断是否是google相册图片
                    uri.lastPathSegment
                }
                isGooglePlayPhotosUri(uri) -> { //判断是否是Google相册图片
                    getImageUrlWithAuthority(context, uri)
                }
                else -> { //其他类似于media这样的图片，和android4.4以下获取图片path方法类似
                    getFilePathBelow19(context, uri)
                }
            }
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.getAuthority()
    }


    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.getAuthority()
    }


    /**
     * @param uri
     * The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.getAuthority()
    }

    /**
     * 判断是否是Google相册的图片，类似于content://com.google.android.apps.photos.content/...
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.getAuthority()
    }


    /**
     * 判断是否是Google相册的图片，类似于content://com.google.android.apps.photos.contentprovider/0/1/mediakey:/local%3A821abd2f-9f8c-4931-bbe9-a975d1f5fabc/ORIGINAL/NONE/1075342619
     */
    private fun isGooglePlayPhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.contentprovider" == uri.getAuthority()
    }

    /**
     * Google相册图片获取路径
     */
    private fun getImageUrlWithAuthority(
        context: Context,
        uri: Uri
    ): String? {
        var `is`: InputStream? = null
        if (uri.authority != null) {
            try {
                `is` = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(`is`)
                return writeToTempImageAndGetPathUri(context, bmp).toString()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } finally {
                try {
                    `is`?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    /**
     * 将图片流读取出来保存到手机本地相册中
     */
    private fun writeToTempImageAndGetPathUri(
        inContext: Context,
        inImage: Bitmap
    ): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    /**
     * 获取小于api19时获取相册中图片真正的uri
     * 对于路径是：content://media/external/images/media/33517这种的，需要转成/storage/emulated/0/DCIM/Camera/IMG_20160807_133403.jpg路径，也是使用这种方法
     * @param context
     * @param uri
     * @return
     */
    private fun getFilePathBelow19(
        context: Context,
        uri: Uri?
    ): String? {
        //这里开始的第二部分，获取图片的路径：低版本的是没问题的，但是sdk>19会获取不到
        var cursor: Cursor? = null
        val path: String?
        try {
            val proj =
                arrayOf(MediaStore.Images.Media.DATA)
            //好像是android多媒体数据库的封装接口，具体的看Android文档
            cursor = context.contentResolver.query(uri!!, proj, null, null, null)
            //获得用户选择的图片的索引值
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            //将光标移至开头 ，这个很重要，不小心很容易引起越界
            cursor?.moveToFirst()
            //最后根据索引值获取图片路径   结果类似：/mnt/sdcard/DCIM/Camera/IMG_20151124_013332.jpg
            path = cursor?.getString(columnIndex ?: 0)
        } finally {
            cursor?.close()
        }
        return path
    }
}