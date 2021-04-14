package com.annora.photo.data

import android.util.Log
import com.annora.photo.common.APP_TAG

class ImageLoader private constructor() {
    companion object {
        private val mInstance: ImageLoader by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ImageLoader()
        }

        fun getInstance() = mInstance
    }

    var allLocalAlbum = arrayListOf<Album>()
    var virtualAlbum = arrayListOf<Album>()
    var allAutoAlbum = arrayListOf<Album>()
    var pendingTitle = ""

    var isUpdate = false

    fun getCurAlbum(isShow: Boolean = true, isAuto: Boolean = false, pos: Int): Album {
        return if (!isShow) allLocalAlbum[0]
        else if (isAuto) allAutoAlbum[pos]
        else if (pos < allLocalAlbum.size) allLocalAlbum[pos]
        else virtualAlbum[pos - allLocalAlbum.size]
    }
}