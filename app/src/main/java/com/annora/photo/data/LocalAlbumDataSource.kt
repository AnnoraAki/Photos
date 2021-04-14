package com.annora.photo.data

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import java.io.File

class LocalAlbumDataSource(
    owner: AppCompatActivity,
    path: String?,
    private val finished: (albums: ArrayList<Album>) -> Unit
) :
    LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        private const val DATA_SOURCE_TAG = "LocalAlbum"
        private const val LOAD_ALL = 222
        private const val LOAD_CATEGORY = 333
        private val IMAGE_PROJECTION =
            arrayOf( //查询图片需要的数据列
                MediaStore.Images.Media.DISPLAY_NAME,  //图片的显示名称  aaa.jpg
                MediaStore.Images.Media.DATA,  //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                MediaStore.Images.Media.SIZE,  //图片的大小，long型  132492
                MediaStore.Images.Media.WIDTH,  //图片的宽度，int型  1920
                MediaStore.Images.Media.HEIGHT,  //图片的高度，int型  1080
                MediaStore.Images.Media.MIME_TYPE,  //图片的类型     image/jpeg
                MediaStore.Images.Media.DATE_ADDED  //图片被添加的时间，long型  1450518608
            )

    }

    private var context: Context = owner
    private var imageFolders = ArrayList<Album>()

    init {
        if (path != null) {
            val bundle = Bundle().apply { putString("path", path) }
            LoaderManager.getInstance(owner).initLoader(LOAD_CATEGORY, bundle, this)
        } else {
            LoaderManager.getInstance(owner).initLoader(LOAD_ALL, null, this)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val path = args?.getString("path") ?: ""
        val selection =
            when (id) {
                LOAD_ALL -> null
                LOAD_CATEGORY -> "${IMAGE_PROJECTION[1]} like '%$path%'"
                else -> throw Exception(
                    "don't find this id"
                )
            }
        val sortOrder = "${IMAGE_PROJECTION[6]} DESC"
        return CursorLoader(
            context,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            IMAGE_PROJECTION,
            selection,
            null,
            sortOrder
        )
    }

    /**
     * 每次回到绑定的activity数据就会进行更新
     */
    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        Log.d(DATA_SOURCE_TAG, "load data start..")
        val newFolders = arrayListOf<Album>()
        if (data != null) {
            val allPictures = arrayListOf<PictureItem>()
            while (data.moveToNext()) {
                val imageName =
                    data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]))
                val imagePath =
                    data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]))

                val file = File(imagePath)
                if (!file.exists() || file.length() <= 0) {
                    continue
                }

                val imageSize =
                    data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]))
                val imageWidth =
                    data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]))
                val imageHeight =
                    data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]))
                val imageMimeType =
                    data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]))
                val imageAddTime =
                    data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]))
                val pictureItem = PictureItem(
                    imageName,
                    imagePath,
                    imageSize,
                    imageWidth,
                    imageHeight,
                    imageMimeType,
                    imageAddTime
                )

                allPictures.add(pictureItem)

                //根据父路径分类存放图片
                val imageFile = File(imagePath)
                val imageParentFile = imageFile.parentFile
                val album = imageParentFile?.let { Album(it.name ?: "", it.path ?: "") } ?: continue
                if (!isSameFolder(newFolders, album)) {
                    val images = arrayListOf<PictureItem>()
                    images.add(pictureItem)
                    newFolders.add(album.apply {
                        cover = pictureItem
                        this.images = images
                    })
                } else {
                    val index = findAlbumIndex(newFolders, album)
                    if (index == -1) {
                        Log.w(
                            DATA_SOURCE_TAG,
                            "find failed, plz check the relationships between albums."
                        )
                        continue
                    }
                    newFolders[index].images.add(pictureItem)
                }
            }

            //防止没有图片报异常
            if (data.count > 0 && allPictures.size > 0) {
                //构造所有图片的集合
                val allImagesFolder = Album("所有照片", "/", allPictures[0], allPictures)
                newFolders.add(0, allImagesFolder) //确保第一条是所有图片

                if (imageFolders.size > 0) Log.d(
                    DATA_SOURCE_TAG,
                    "check new :${newFolders[0].images.size} / pre : ${imageFolders[0].images.size}"
                )
                if (newFolders != imageFolders) imageFolders = newFolders

                Log.d(
                    DATA_SOURCE_TAG,
                    "finish : folds num = ${imageFolders.size} / pictures num = $allPictures"
                )
            } else {
                Log.d(DATA_SOURCE_TAG, "finish : no data loaded.")
                Log.d(DATA_SOURCE_TAG, "folds num = ${imageFolders.size}")
            }
        }
        finished.invoke(imageFolders)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        Log.d(DATA_SOURCE_TAG, "reset loader")
    }

    private fun isSameFolder(folders: ArrayList<Album>, album: Album): Boolean {
        for (a: Album in folders) {
            if (a.path == album.path) return true
        }
        return false
    }

    private fun findAlbumIndex(folders: ArrayList<Album>, album: Album): Int {
        folders.forEachIndexed { index, a ->
            if (a.path == album.path) return index
        }
        return -1
    }
}