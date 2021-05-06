package com.annora.photo.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.annora.photo.R
import com.annora.photo.common.APP_TAG
import com.annora.photo.data.ImageLoader
import com.annora.photo.data.PictureItem
import com.annora.photo.ui.adapter.DetailsAdapter
import com.annora.photo.viewmodel.PhotoDetailsViewModel
import com.annora.photo.utils.*
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_photo_details.*
import kotlinx.android.synthetic.main.popupwindow_pic_details.view.*
import java.io.File

class PhotoDetailsActivity : AppCompatActivity() {

    companion object {
        const val DETAILS_ALBUM_POS = "details_album_pos"
        const val DETAILS_ALBUM_IS_AUTO = "details_album_is_auto"
        const val DETAILS_PIC_POS = "details_pic_pos"
    }

    private val albumPos by lazy(LazyThreadSafetyMode.NONE) {
        intent.getIntExtra(
            DETAILS_ALBUM_POS,
            0
        )
    }
    private val isAuto by lazy(LazyThreadSafetyMode.NONE) {
        intent.getBooleanExtra(
            DETAILS_ALBUM_IS_AUTO,
            false
        )
    }

    private val picsViewModel by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this).get(PhotoDetailsViewModel::class.java)
    }
    private val picAdapter = DetailsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_details)
        picsViewModel.curPos = intent.getIntExtra(DETAILS_PIC_POS, 0)
        picsViewModel.photos.value =
            ImageLoader.getInstance().getCurAlbum(isAuto = isAuto, pos = albumPos).images
        init()
    }

    private fun init() {
        picsViewModel.photos.observe(this, Observer {
            picAdapter.updateData(it)
            picsViewModel.originData = it
            vp_show_details.setCurrentItem(picsViewModel.curPos, false)
        })

        vp_show_details.apply {
            adapter = picAdapter
            setCurrentItem(picsViewModel.curPos, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    picsViewModel.curPos = position
                }
            })
        }

        tb_show_details.setNavigationOnClickListener { finish() }
        tb_show_details.setOnMenuItemClickListener {
            cropCurPic()
            true
        }

        iv_see_details.setOnClickListener {
            showDetailsWindow()
        }
    }

    private fun cropCurPic() {
        val curPhoto = picsViewModel.getCurPhoto()
        val preUri = Uri.fromFile(File(curPhoto.path))
        val newFile = File(getOriginPath() + File.separator + getTimeStamp() + ".jpg")
        val parentFile = File(getOriginPath())
        if (!parentFile.exists()) parentFile.mkdir()
        val newUri = Uri.fromFile(newFile)
        val options = UCrop.Options().apply {
            setToolbarWidgetColor(ActivityCompat.getColor(this@PhotoDetailsActivity, R.color.white))
            setToolbarColor(ActivityCompat.getColor(this@PhotoDetailsActivity, R.color.blue_light))
            setStatusBarColor(ActivityCompat.getColor(this@PhotoDetailsActivity, R.color.blue_dark))
            setFreeStyleCropEnabled(true)
        }
        UCrop.of(preUri, newUri)
            .withMaxResultSize(curPhoto.width, curPhoto.height) // ignore this error :)
            .withOptions(options)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data ?: return
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data) ?: return
            Log.d(APP_TAG, "add new pic :" + Uri2FileRealPathUtils.getPath(this, resultUri))
            noticeLocalAndAdd(resultUri)
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data)
            Log.w(APP_TAG, cropError.toString())
        }
    }

    private fun noticeLocalAndAdd(resultUri: Uri) {
        // 添加到指定的文件夹（如果不存在则创建）和所有图片里
        // 保证该uri对应的文件存在后再通知
        val filePath = Uri2FileRealPathUtils.getPath(this, resultUri) ?: return
        val arrays = filePath.split('/')
        val name = arrays[arrays.size - 1]
        val bitmap = BitmapFactory.decodeFile(filePath)
        val item =
            PictureItem(
                name = name,
                path = filePath,
                width = bitmap.width,
                height = bitmap.height,
                size = bitmap.allocationByteCount.toLong(),
                type = "image/jpeg",
                addTime = name.split(".")[0].toLong()
            )
        if (!bitmap.isRecycled) bitmap.recycle()
        // 通知相册内容更新
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = resultUri
        sendBroadcast(mediaScanIntent)

        ImageLoader.getInstance().allLocalAlbum[0].images.add(0, item)
        ImageLoader.getInstance().isUpdate = true

        picsViewModel.originData.add(0, item)
        picsViewModel.photos.postValue(picsViewModel.originData)
    }

    // todo when done all other need..
    //  i can do it.. :(
    private fun goToEditActivity() {
        val curPhoto = picsViewModel.getCurPhoto()
        val intent = Intent(this, PhotoEditActivity::class.java)
        intent.putExtra(PhotoEditActivity.PHOTO_PATH, curPhoto.path)
        startActivity(intent)
    }

    private fun showDetailsWindow() {
        val curPhoto = picsViewModel.getCurPhoto()
        val rootView = LayoutInflater.from(this@PhotoDetailsActivity)
            .inflate(R.layout.activity_photo_details, null)
        val view = LayoutInflater.from(this@PhotoDetailsActivity)
            .inflate(R.layout.popupwindow_pic_details, null).apply {
                tv_photo_name.text = curPhoto.name
                tv_path.text = curPhoto.path
                tv_size_num.text = formatPicSize(curPhoto.width, curPhoto.height)
                tv_size.text = formatSize(curPhoto.size)
            }
        setBackgroundAlpha(0.6f)
        PopupWindow(this).apply {
            contentView = view
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            isOutsideTouchable = false
            isFocusable = true
            setBackgroundDrawable(ColorDrawable(0x00000000))
            showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
            setOnDismissListener {
                setBackgroundAlpha(1f)
            }
        }
    }
}