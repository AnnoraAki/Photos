package com.annora.photo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.annora.photo.R
import com.annora.photo.data.Album
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rv_files_item.view.*

/**
 * warning : 初始化一定要在load完成后进行，否则数据无法更新
 * 有能力把这边数据流改成观察者模式 / 或者数据更新通知这边刷新
 */
class AlbumsAdapter(
    private val onClicked: (pos : Int) -> Unit
) :
    RecyclerView.Adapter<AlbumsAdapter.AlbumsViewHolder>() {

    private var albums = arrayListOf<Album>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rv_files_item, parent, false)
        return AlbumsViewHolder(view)
    }

    override fun getItemCount() = albums.size

    override fun onBindViewHolder(holder: AlbumsViewHolder, position: Int) {
        val album = albums[position]
        holder.itemView.apply {
            Glide.with(this).load(album.cover.path).into(iv_file_photo)
            tv_photo_title.text = album.name
            tv_photo_num.text = "${album.images.size}"
            setOnClickListener { onClicked(position) }
        }
    }

    fun dataUpdate(newAlbums: ArrayList<Album>) {
        albums = newAlbums
        notifyDataSetChanged()
    }

    inner class AlbumsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}