package com.annora.photo.ui.activity

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.annora.photo.R
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_photo_edit.*

/**
 * 等我有时间再仔细搞..
 */
class PhotoEditActivity : AppCompatActivity() {

    companion object {
        const val PHOTO_PATH = "edit_photo_path"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)

        val path = intent.getStringExtra(PHOTO_PATH)

    }
}