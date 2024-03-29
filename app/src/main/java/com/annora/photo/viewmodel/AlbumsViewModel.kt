package com.annora.photo.viewmodel

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annora.photo.common.BaseApp
import com.annora.photo.common.SAVE_LOCAL_NUM
import com.annora.photo.common.TEST_TAG
import com.annora.photo.data.*
import com.annora.photo.utils.defaultSharedPreferences
import com.annora.photo.utils.editor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlbumsViewModel : ViewModel() {
    val localAlbumsLiveData = MutableLiveData<ArrayList<Album>>()
    val virtualAlbumsLiveData = MutableLiveData<ArrayList<Album>>()
    val allAutoAlbumsLiveData = MutableLiveData<ArrayList<Album>>()
    val shouldLoadAutoLiveData = MutableLiveData<Boolean>()
    private lateinit var dataSourceLocal: LocalAlbumDataSource

    fun initLocal(owner: AppCompatActivity) {
        dataSourceLocal = LocalAlbumDataSource(owner, null) {
            localAlbumsLiveData.value = it
            ImageLoader.getInstance().allLocalAlbum = it
            initLocalVirtualAlbums()
            loadAutoAlbums()
        }
    }

    fun initAuto() {
        Thread(Runnable {
            Log.d(TEST_TAG, "init auto")
            LoadAutoAlbumDataSource.startLoad {
                allAutoAlbumsLiveData.postValue(it)
                saveAutoAlbums()
            }
        }).start()
    }

    fun checkData() {
        if (ImageLoader.getInstance().isUpdate) {
            virtualAlbumsLiveData.value = ImageLoader.getInstance().virtualAlbum
            ImageLoader.getInstance().isUpdate = false
        }
    }

    fun close() {
        LoadAutoAlbumDataSource.close()
    }

    private fun initLocalVirtualAlbums() {
        viewModelScope.launch(Dispatchers.IO) {
            val res =
                VirtualAlbumDataBase.getInstance().virtualAlbumDao().getAllVirtualLocalAlbums()
            val virtual = ArrayList(res)
            virtualAlbumsLiveData.postValue(virtual)
        }
    }

    private fun saveAutoAlbums() {
        if (BaseApp.IS_TF_DEBUG) return
        viewModelScope.launch(Dispatchers.IO) {
            VirtualAlbumDataBase.getInstance().virtualAlbumDao().deleteAllAutoAlbum()
            ImageLoader.getInstance().allAutoAlbum.forEach {
                VirtualAlbumDataBase.getInstance().virtualAlbumDao().addVirtualAlbum(it)
            }
        }
    }

    private fun loadAutoAlbums() {
        if (BaseApp.IS_TF_DEBUG) {
            shouldLoadAutoLiveData.postValue(true)
            // 防止重复触发
            BaseApp.IS_TF_DEBUG = false
            return
        }
        val preNum = BaseApp.context.defaultSharedPreferences.getInt(SAVE_LOCAL_NUM, 0)
        val curNum = ImageLoader.getInstance().allLocalAlbum[0].images.size
        Log.d(TEST_TAG, "pre num : $preNum ｜ cur num : $curNum")
        if (preNum != curNum) {
            shouldLoadAutoLiveData.postValue(true)
            BaseApp.context.defaultSharedPreferences.editor {
                putInt(SAVE_LOCAL_NUM, curNum)
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val res = VirtualAlbumDataBase.getInstance().virtualAlbumDao().getAllVirtualAutoAlbums()
            if (res.isNotEmpty()) {
                val wrapper = ArrayList(res)
                allAutoAlbumsLiveData.postValue(wrapper)
                shouldLoadAutoLiveData.postValue(false)
            } else {
                shouldLoadAutoLiveData.postValue(true)
            }
        }
    }
}