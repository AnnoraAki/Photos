package com.annora.photo.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PictureItemsConverters {
    @TypeConverter
    fun stringToObject(value: String): ArrayList<PictureItem> {
        val listType = object : TypeToken<List<PictureItem>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun objectToString(list: ArrayList<PictureItem>): String {
        return Gson().toJson(list)
    }
}