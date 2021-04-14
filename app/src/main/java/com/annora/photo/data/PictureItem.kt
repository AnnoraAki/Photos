package com.annora.photo.data

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "pics_details")
data class PictureItem(
    @PrimaryKey
    @ColumnInfo(name = "details_name")
    val name: String = "",
    @ColumnInfo(name = "details_pic_path")
    val path: String = "",
    @ColumnInfo(name = "details_size")
    val size: Long = 0L,
    @ColumnInfo(name = "details_pic_width")
    val width: Int = 0,
    @ColumnInfo(name = "details_pic_height")
    val height: Int = 0,
    @ColumnInfo(name = "details_type")
    val type: String = "",
    @ColumnInfo(name = "details_add_time")
    val addTime: Long = 0L
) : Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "details_id")
    var id = 0

    // 方便记录每个item的选中状态
    @Ignore
    var selected = false

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(path)
        parcel.writeLong(size)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeString(type)
        parcel.writeLong(addTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PictureItem> {
        override fun createFromParcel(parcel: Parcel): PictureItem {
            return PictureItem(parcel)
        }

        override fun newArray(size: Int): Array<PictureItem?> {
            return arrayOfNulls(size)
        }
    }
}