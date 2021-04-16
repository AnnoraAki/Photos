package com.annora.photo.common

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.annora.photo.data.VirtualAlbumDataBase

class BaseApp : Application() {

    companion object {
        lateinit var context: Context
            private set
        var IS_TF_DEBUG = false
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        context = base
    }

}