package com.annora.photo.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.annora.photo.R
import com.annora.photo.data.PictureItem
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.rv_photo_details_item.view.*

class DetailsAdapter :
    RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder>() {

    private var pictures : ArrayList<PictureItem> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_photo_details_item, parent, false)
        return DetailsViewHolder(view)
    }

    override fun getItemCount() = pictures.size

    override fun onBindViewHolder(holder: DetailsViewHolder, position: Int) {
        val pic = pictures[position]
        holder.itemView.apply {
            Glide.with(this).load(pic.path).into(iv_photo_details)
        }
    }

    fun updateData(pics : ArrayList<PictureItem>) {
        pictures = pics
        notifyDataSetChanged()
    }

    inner class DetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}