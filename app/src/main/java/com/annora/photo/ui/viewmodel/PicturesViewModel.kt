package com.annora.photo.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annora.photo.data.Album
import com.annora.photo.data.ImageLoader
import com.annora.photo.data.PictureItem
import com.annora.photo.data.VirtualAlbumDataBase
import com.annora.photo.utils.encodeForPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PicturesViewModel : ViewModel() {
    val picturesLiveData = MutableLiveData<ArrayList<PictureItem>>()

    // 防止多次点击导致重复添加
    private var isAdding = false

    fun addNewAlbum(pics: ArrayList<PictureItem>) {
        if (isAdding) return
        isAdding = true
        val name = ImageLoader.getInstance().pendingTitle
        val new = Album(name, encodeForPath(name), pics[pics.size - 1], pics)
        ImageLoader.getInstance().virtualAlbum.add(new)
        viewModelScope.launch(Dispatchers.IO) {
            VirtualAlbumDataBase.getInstance().virtualAlbumDao().addVirtualAlbum(new)
        }
        isAdding = false
    }

    fun checkData(album: Album) {
        if (ImageLoader.getInstance().isUpdate) {
            if (album.name == "所有照片") {
                picturesLiveData.postValue(ImageLoader.getInstance().allLocalAlbum[0].images)
            }
        }
    }
}