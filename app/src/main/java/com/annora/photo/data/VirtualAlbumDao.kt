package com.annora.photo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface VirtualAlbumDao {
    @Insert
    fun addVirtualAlbum(album : Album)

    @Delete
    fun deleteVirtualAlbum(vararg albums: Album)

    @Query("DELETE FROM albums WHERE album_is_auto = 1")
    fun deleteAllAutoAlbum()

    @Query("SELECT * FROM albums WHERE album_is_auto = 0")
    fun getAllVirtualLocalAlbums() : List<Album>

    @Query("SELECT * FROM albums WHERE album_is_auto = 1")
    fun getAllVirtualAutoAlbums() : List<Album>
}