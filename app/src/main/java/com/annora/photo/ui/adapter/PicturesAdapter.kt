package com.annora.photo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.annora.photo.R
import com.annora.photo.utils.invisible
import com.annora.photo.utils.visible
import com.annora.photo.data.PictureItem
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rv_photo_item.view.*

/**
 * warning : 相册内容更新时需同步更新
 */

class PicturesAdapter(private val onClicked: (pos: Int) -> Unit) :
    RecyclerView.Adapter<PicturesAdapter.PicturesViewHolder>() {

    private var pictures = arrayListOf<PictureItem>()
    var isShowMode = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PicturesViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rv_photo_item, parent, false)
        return PicturesViewHolder(view)
    }

    override fun getItemCount() = pictures.size

    override fun onBindViewHolder(holder: PicturesViewHolder, position: Int) {
        val pic = pictures[position]
        holder.itemView.apply {
            if (isShowMode) cb_choose.invisible() else cb_choose.visible()
            Glide.with(this).load(pic.path).into(iv_photo)
            setOnClickListener {
                if (isShowMode) onClicked(position)
                else {
                    pic.selected = !pic.selected
                    cb_choose.isChecked = pic.selected
                }
            }
            cb_choose.setOnClickListener {
                pic.selected = cb_choose.isChecked
            }
        }
    }

    fun dataUpdate(newPics: ArrayList<PictureItem>) {
        pictures = newPics
        notifyDataSetChanged()
    }

    fun getSelectedPhotos(): List<PictureItem> {
        val res = pictures.filter { it.selected }
        pictures.forEach {
            it.selected = false
        }
        return res
    }

    inner class PicturesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}