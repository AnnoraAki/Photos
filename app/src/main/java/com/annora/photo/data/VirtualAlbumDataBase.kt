package com.annora.photo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.annora.photo.BuildConfig
import com.annora.photo.common.BaseApp

@Database(entities = [Album::class], version = 1)
abstract class VirtualAlbumDataBase : RoomDatabase() {
    abstract fun virtualAlbumDao(): VirtualAlbumDao

    companion object {
        private var INSTANCE: VirtualAlbumDataBase? = null

        fun getInstance(): VirtualAlbumDataBase {
            if (INSTANCE == null) {
                synchronized(VirtualAlbumDataBase::class.java) {

                    // 生成数据库文件
                    val builder = Room.databaseBuilder(
                        BaseApp.context,
                        VirtualAlbumDataBase::class.java, "album-database"
                    )

                    if (!BuildConfig.DEBUG) {
                        // 迁移数据库如果发生错误，将会重新创建数据库，而不是发生崩溃
                        builder.fallbackToDestructiveMigration()
                    }
                    INSTANCE = builder.build()
                }
            }
            return INSTANCE!!
        }
    }
}