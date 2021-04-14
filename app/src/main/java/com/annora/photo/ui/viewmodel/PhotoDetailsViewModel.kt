package com.annora.photo.ui.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.annora.photo.data.PictureItem

class PhotoDetailsViewModel : ViewModel() {
    var curPos = 0
    var photos = MutableLiveData<ArrayList<PictureItem>>()

    // warning : 数据需跟photos同步
    // 该处理仅仅防止数据无法读取的情况
    var originData = arrayListOf<PictureItem>()


    fun getCurPhoto() = originData[curPos]

}