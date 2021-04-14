package com.annora.photo.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import java.io.Serializable

@Entity(tableName = "albums")
@TypeConverters(PictureItemsConverters::class)
data class Album(
    @ColumnInfo(name = "album_name")
    val name: String = "",
    @ColumnInfo(name = "album_path")
    val path: String = "",
    @Embedded
    var cover: PictureItem = PictureItem(),
    var images: ArrayList<PictureItem> = arrayListOf(),
    @ColumnInfo(name = "album_is_auto")
    var isAuto : Boolean = false
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "album_id")
    var id = 0

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Thread.currentThread().contextClassLoader) ?: PictureItem(),
        parcel.createTypedArrayList(PictureItem.CREATOR) ?: arrayListOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeParcelable(cover, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Album> {
        override fun createFromParcel(parcel: Parcel): Album {
            return Album(parcel)
        }

        override fun newArray(size: Int): Array<Album?> {
            return arrayOfNulls(size)
        }
    }

}