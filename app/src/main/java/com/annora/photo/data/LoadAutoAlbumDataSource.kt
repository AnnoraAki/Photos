package com.annora.photo.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import com.annora.photo.common.BaseApp
import com.annora.photo.common.IMG_SIZE
import com.annora.photo.common.TEST_TAG
import com.annora.photo.tensorflow.Classifier
import com.annora.photo.tensorflow.ClassifierFloatMobileNet
import com.annora.photo.utils.encodeForPath
import java.util.*

object LoadAutoAlbumDataSource {
    private var classifier: Classifier? = null
    private var autoAlbums = ImageLoader.getInstance().allAutoAlbum

    /**
     * 保证本地的所有照片数据被读取后再调用该方法
     */
    fun startLoad(finished: (albums: ArrayList<Album>) -> Unit) {
        checkClassifierIsRunning()
        if (!checkClassifierIsNull()) {
            Log.d(TEST_TAG, "start loading..")
            if (checkLocalAlbumIsLoaded()) {
                ImageLoader.getInstance().allLocalAlbum[0].images.forEach {
                    val loadBitmap = BitmapFactory.decodeFile(it.path)
                    if (loadBitmap.width < classifier?.imageSizeX ?: IMG_SIZE || loadBitmap.height < classifier?.imageSizeX ?: IMG_SIZE) {
                        loadBitmap.recycle()
                    } else {
                        val changeBitmap = changeBitmapSize(
                            loadBitmap,
                            classifier?.imageSizeX ?: IMG_SIZE,
                            classifier?.imageSizeY ?: IMG_SIZE
                        )
                        val label = classifier?.classifyFrame(changeBitmap)
                        loadBitmap.recycle()
                        changeBitmap.recycle()
                        if (label != null) classifyIntoAlbum(it, label)
                    }
                }
                Log.d(TEST_TAG, "classify is done :>")
                finished.invoke(autoAlbums)
            } else {
                Log.w(TEST_TAG, "local album is not loaded.")
            }
        } else {
            Log.d(TEST_TAG, "classifier is null .. ?")
        }
    }

    private fun classifyIntoAlbum(item: PictureItem, labels: Array<String>) {
        Log.d(TEST_TAG, "${item.path} : ${labels.contentToString()}")
        val tag = labels[0]
        val chance = labels[1]
//        if (chance.toFloat() < 0.8f) return
        val index = findAlbum(tag)
        Log.d(TEST_TAG, "$tag -- $index")
        if (index == -1) {
            val images = arrayListOf<PictureItem>()
            images.add(item)
            val album = Album(
                name = tag,
                path = encodeForPath(tag),
                cover = item,
                images = images,
                isAuto = true
            )
            autoAlbums.add(album)
        } else {
            Log.d(
                TEST_TAG,
                "${autoAlbums[index].name} images : ${autoAlbums[index].images}"
            )
            autoAlbums[index].images.add(item)
        }
    }

    private fun findAlbum(tag: String): Int {
        autoAlbums.forEachIndexed { index, album ->
            if (album.name == tag) return index
        }
        return -1
    }

    private fun changeBitmapSize(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        // 获得图片的宽高.
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        // 计算缩放比例.
        val scaleWidth = width * 1.0f / bitmapWidth
        val scaleHeight = height * 1.0f / bitmapHeight
        // 取得想要缩放的matrix参数.
        val matrix = Matrix().apply {
            postScale(scaleWidth, scaleHeight)
        }
        // 得到新的图片.
        return Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true)
    }

    private fun checkLocalAlbumIsLoaded() = ImageLoader.getInstance().allLocalAlbum.size != 0

    private fun checkClassifierIsNull() = classifier == null

    private fun checkClassifierIsRunning() {
        if (classifier != null) {
            classifier?.close()
            classifier = null
        }
        classifier = ClassifierFloatMobileNet(BaseApp.context).apply { useCPU() }
    }
}