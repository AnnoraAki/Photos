package com.annora.photo.ui.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.annora.photo.R
import com.annora.photo.common.APP_TAG
import com.annora.photo.data.Album
import com.annora.photo.data.ImageLoader
import com.annora.photo.ui.NewFolderDialog
import com.annora.photo.ui.adapter.AlbumsAdapter
import com.annora.photo.ui.viewmodel.AlbumsViewModel
import com.annora.photo.utils.PermissionHelper
import com.annora.photo.utils.toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val albumsViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(AlbumsViewModel::class.java)
    }

    private val localAlbumAdapter = AlbumsAdapter { goDetailsActivity(it, false) }
    private val autoAlbumAdapter = AlbumsAdapter { goDetailsActivity(it, true) }
    private val newFolderDialog by lazy(LazyThreadSafetyMode.NONE) {
        NewFolderDialog(this) {
            createNewFolder(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAndRequestPermission()
        init()
    }

    override fun onResume() {
        super.onResume()
        albumsViewModel.checkData()
    }

    private fun checkAndRequestPermission() {
        val code = PermissionHelper.checkWritePermission(this@MainActivity)
        if (code != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(PermissionHelper.PERMISSION_WRITE),
                PermissionHelper.PERMISSION_WRITE_CODE
            )
        } else {
            Log.d(APP_TAG, "permission is ok")
            albumsViewModel.initLocal(this)
        }
    }

    private fun init() {
        albumsViewModel.localAlbumsLiveData.observe(this@MainActivity, Observer {
            if (it.size <= 0) return@Observer
            ImageLoader.getInstance().allLocalAlbum = it
            updateLocalData()
        })
        albumsViewModel.virtualAlbumsLiveData.observe(this@MainActivity, Observer {
            if (it.size <= 0) return@Observer
            ImageLoader.getInstance().virtualAlbum = it
            updateLocalData()
        })
        albumsViewModel.allAutoAlbumsLiveData.observe(this@MainActivity, Observer {
            if (it.size <= 0) return@Observer
            ImageLoader.getInstance().allAutoAlbum = it
            autoAlbumAdapter.dataUpdate(it)
        })
        initRecycleData()

        albumsViewModel.shouldLoadAutoLiveData.observe(this@MainActivity, Observer {
            if (it) albumsViewModel.initAuto()
        })

        tb_main.setOnMenuItemClickListener {
            newFolderDialog.show()
            true
        }
    }


    private fun initRecycleData() {
        rv_mine.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = localAlbumAdapter
        }

        rv_auto.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }
            adapter = autoAlbumAdapter
        }
    }

    private fun goDetailsActivity(pos: Int, isAuto: Boolean, isShow: Boolean = true) {
        val intent = Intent(this, PicturesActivity::class.java)
        intent.putExtra(PicturesActivity.PIC_ACTIVITY_TAG, pos)
        intent.putExtra(PicturesActivity.PIC_IS_AUTO, isAuto)
        intent.putExtra(PicturesActivity.PIC_MODE, isShow)
        startActivity(intent)
    }

    private fun updateLocalData() {
        val newList = ArrayList<Album>().apply {
            addAll(ImageLoader.getInstance().allLocalAlbum)
            addAll(ImageLoader.getInstance().virtualAlbum)
        }
        localAlbumAdapter.dataUpdate(newList)
    }

    private fun createNewFolder(name: String) {
        ImageLoader.getInstance().pendingTitle = name
        goDetailsActivity(0, isAuto = false, isShow = false)
        newFolderDialog.dismiss()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermissionHelper.PERMISSION_WRITE_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    toast("你拒绝了权限申请，可能导致无法修改图片或图片分类")
                } else {
                    albumsViewModel.initLocal(this)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        albumsViewModel.close()
    }
}