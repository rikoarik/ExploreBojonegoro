package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gracedian.explorebojonegoro.R

class GaleriAdapter(private val galeriList: List<String>) : RecyclerView.Adapter<GaleriAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.galery_items, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val galeri = galeriList[position]
        Glide.with(holder.itemView)
            .load(galeri)
            .into(holder.imgGaleri)
    }

    override fun getItemCount(): Int {
        return galeriList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgGaleri: ImageView = itemView.findViewById(R.id.imgGaleri)
    }
}
