package com.gracedian.explorebojonegoro.ui.dashboard.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.PopularItem

class PopularAdapter(
    private val popularItems: List<PopularItem>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {

    interface OnItemClickListener {
        fun onItemPopularClick(position: Int)
    }

    class PopularViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgWisata = itemView.findViewById<ImageView>(R.id.imgWisata)
        val nameWisata = itemView.findViewById<TextView>(R.id.nameWisata)
        val locWisata = itemView.findViewById<TextView>(R.id.locWisata)
        val jaraktxt = itemView.findViewById<TextView>(R.id.jaraktxt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.popular_items, parent, false)
        return PopularViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        val currentItem = popularItems[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .into(holder.imgWisata)

        holder.nameWisata.text = currentItem.namaWisata
        holder.locWisata.text = currentItem.lokasiWisata
        holder.jaraktxt.text = currentItem.rating.toString() + " "

        holder.itemView.setOnClickListener {
            itemClickListener.onItemPopularClick(position)
        }
    }

    override fun getItemCount() = Integer.min(popularItems.size, 5)
}
