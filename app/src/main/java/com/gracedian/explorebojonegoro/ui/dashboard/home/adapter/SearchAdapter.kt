package com.gracedian.explorebojonegoro.ui.dashboard.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.SearchItem

class SearchAdapter(
    private val searchList: MutableList<SearchItem>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<SearchAdapter.SearchAdapterViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onFavoriteClick(position: Int)
    }

    class SearchAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgWisata = itemView.findViewById<ImageView>(R.id.imgWisata)
        val namaWisata = itemView.findViewById<TextView>(R.id.namaWisata)
        val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
        val locWisata = itemView.findViewById<TextView>(R.id.locWisata)
        val txtjarak = itemView.findViewById<TextView>(R.id.txtjarak)
        val btFavorite = itemView.findViewById<ImageView>(R.id.btFavorite)
    }

    fun setItems(newItems: List<SearchItem>) {
        searchList.clear()
        searchList.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchAdapterViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_items, parent, false)
        return SearchAdapterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchAdapterViewHolder, position: Int) {
        val currentItem = searchList[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .into(holder.imgWisata)

        holder.namaWisata.text = currentItem.wisata
        holder.ratingBar.rating = currentItem.rating!!.toFloat()
        holder.locWisata.text = currentItem.alamat
        holder.txtjarak.text = "${currentItem.jarak} Km"

        // Set an OnClickListener for the btFavorite button

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(position)
        }
        holder.btFavorite.setOnClickListener {
            itemClickListener.onFavoriteClick(position)
        }
    }

    override fun getItemCount() = searchList.size
}
