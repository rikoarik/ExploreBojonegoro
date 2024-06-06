package com.gracedian.explorebojonegoro.ui.dashboard.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import java.lang.Integer.min

class WisataTerdekatAdapter(
    private var wisataTerdekatList: MutableList<WisataTerdekatItem>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<WisataTerdekatAdapter.WisataTerdekatViewHolder>() {

    interface OnItemClickListener {
        fun onItemTerdekatClick(position: Int)
        fun onFavoriteClick(position: Int)
    }


    class WisataTerdekatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgWisata = itemView.findViewById<ImageView>(R.id.imgWisata)
        val namaWisata = itemView.findViewById<TextView>(R.id.namaWisata)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
        val locWisata = itemView.findViewById<TextView>(R.id.locWisata)
        val txtjarak = itemView.findViewById<TextView>(R.id.txtjarak)
        val btFavorite = itemView.findViewById<ImageView>(R.id.btFavorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WisataTerdekatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.wisata_terdekat_items, parent, false)
        return WisataTerdekatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WisataTerdekatViewHolder, position: Int) {
        val currentItem = wisataTerdekatList[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .apply(
                RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .timeout(60000))
            .into(holder.imgWisata)

        holder.namaWisata.text = currentItem.wisata
        holder.ratingBar.rating = currentItem.rating!!.toFloat()
        holder.locWisata.text = currentItem.alamat
        holder.txtjarak.text = "${currentItem.jarak} Km"


        holder.btFavorite.setOnClickListener {
            itemClickListener.onFavoriteClick(position)
        }
        holder.itemView.setOnClickListener {
            itemClickListener.onItemTerdekatClick(position)
        }
    }

    override fun getItemCount() = min(wisataTerdekatList.size, 5)

    fun setItems(newItems: List<WisataTerdekatItem>) {
        wisataTerdekatList.clear()
        wisataTerdekatList.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): WisataTerdekatItem {
        return wisataTerdekatList[position]
    }

}
