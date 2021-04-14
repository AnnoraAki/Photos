package com.annora.photo.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.annora.photo.R
import com.annora.photo.data.Album
import com.annora.photo.data.ImageLoader
import com.annora.photo.ui.adapter.PicturesAdapter
import com.annora.photo.ui.viewmodel.PicturesViewModel
import kotlinx.android.synthetic.main.activity_pictures.*

class PicturesActivity : AppCompatActivity() {

    companion object {
        const val PIC_ACTIVITY_TAG = "pictures"
        const val PIC_MODE = "mode"
        const val PIC_IS_AUTO = "isAuto"
    }

    private lateinit var album: Album

    // 如果是展示模式为true
    private val showMode by lazy(LazyThreadSafetyMode.NONE) {
        intent.getBooleanExtra(
            PIC_MODE,
            true
        )
    }
    private val isAuto by lazy(LazyThreadSafetyMode.NONE) {
        intent.getBooleanExtra(
            PIC_IS_AUTO,
            false
        )
    }
    private val pos by lazy(LazyThreadSafetyMode.NONE) { intent.getIntExtra(PIC_ACTIVITY_TAG, 0) }
    private val picsAdapter = PicturesAdapter { seePhotoDetails(it) }
    private val picsViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(
            PicturesViewModel::class.java
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pictures)

        album = ImageLoader.getInstance().getCurAlbum(showMode, isAuto, pos)
        init()
    }

    override fun onResume() {
        super.onResume()
        picsViewModel.checkData(album)
    }

    private fun init() {
        rv_detail.apply {
            layoutManager = GridLayoutManager(this@PicturesActivity, 4)
            adapter = picsAdapter.apply { this.isShowMode = showMode }
        }

        tlb_details.apply {
            setNavigationOnClickListener { finish() }
            title = if (!showMode) album.name else "${album.name}(${album.images.size}张)"
            if (!showMode) {
                inflateMenu(R.menu.choose_menu)
                setOnMenuItemClickListener {
                    picsViewModel.addNewAlbum(ArrayList(picsAdapter.getSelectedPhotos()))
                    finish()
                    true
                }
            }
        }

        picsViewModel.picturesLiveData.apply {
            observe(this@PicturesActivity, Observer {
                picsAdapter.dataUpdate(it)
                album.images = it
            })
            value = album.images
        }
    }

    private fun seePhotoDetails(picPos: Int) {
        val intent = Intent(this@PicturesActivity, PhotoDetailsActivity::class.java)
        intent.putExtra(PhotoDetailsActivity.DETAILS_ALBUM_IS_AUTO, isAuto)
        intent.putExtra(PhotoDetailsActivity.DETAILS_ALBUM_POS, pos)
        intent.putExtra(PhotoDetailsActivity.DETAILS_PIC_POS, picPos)
        startActivity(intent)
    }
}