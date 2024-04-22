package com.gracedian.explorebojonegoro.ui.dashboard.profile.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import com.gracedian.explorebojonegoro.ui.dashboard.profile.item.HistoryTrips
import java.lang.Integer.min

class HistoryTripsAdapter(
    private val wisataTerdekatList: List<HistoryTrips>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<HistoryTripsAdapter.HistoryTripsViewHolder>() {

    interface OnItemClickListener {
        fun onItemTerdekatClick(position: Int)
    }

    class HistoryTripsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgWisata = itemView.findViewById<ImageView>(R.id.imgWisata)
        val namaWisata = itemView.findViewById<TextView>(R.id.namaWisata)
        val locWisata = itemView.findViewById<TextView>(R.id.locWisata)
        val txtDateHistory = itemView.findViewById<TextView>(R.id.txtDateHistory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryTripsViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_trip_item, parent, false)
        return HistoryTripsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryTripsViewHolder, position: Int) {
        val currentItem = wisataTerdekatList[position]

        Glide.with(holder.itemView.context)
            .load(currentItem.imageUrl)
            .into(holder.imgWisata)

        holder.namaWisata.text = currentItem.wisata
        holder.locWisata.text = currentItem.alamat
        holder.txtDateHistory.text = currentItem.date


        holder.itemView.setOnClickListener {
            itemClickListener.onItemTerdekatClick(position)
        }
    }

    override fun getItemCount() = min(wisataTerdekatList.size, 5)



}
