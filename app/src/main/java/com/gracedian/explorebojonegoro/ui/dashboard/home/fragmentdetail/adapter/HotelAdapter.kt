package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.Hotel

class HotelAdapter(private val hotelList: List<Hotel>, private val onItemClick: (Hotel) -> Unit) : RecyclerView.Adapter<HotelAdapter.HotelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.terdekat_items, parent, false)
        return HotelViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        val hotel = hotelList[position]
        holder.bind(hotel)
    }

    override fun getItemCount(): Int {
        return hotelList.size
    }

    class HotelViewHolder(itemView: View, private val onItemClick: (Hotel) -> Unit) : RecyclerView.ViewHolder(itemView) {
        fun bind(hotel: Hotel) {

            itemView.findViewById<TextView>(R.id.namaPenginapan).text = hotel.nama
            itemView.findViewById<TextView>(R.id.locPenginapan).text = hotel.alamat
            var ratingBar = itemView.findViewById<RatingBar>(R.id.ratingBar)
            ratingBar.rating = hotel.rating!!.toFloat()

            Glide.with(itemView.context)
                .load(hotel.imageUrl)
                .into(itemView.findViewById(R.id.imgPenginapan))
            itemView.findViewById<TextView>(R.id.txtjarak).text = " ${hotel.jarak} Km"
            itemView.setOnClickListener {
                onItemClick(hotel)
            }
        }
    }
}

