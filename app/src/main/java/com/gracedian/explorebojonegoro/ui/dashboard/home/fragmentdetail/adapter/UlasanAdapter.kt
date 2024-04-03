package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.UlasanItems
import de.hdodenhof.circleimageview.CircleImageView

class UlasanAdapter(private val ulasanList: List<UlasanItems>) :
    RecyclerView.Adapter<UlasanAdapter.UlasanViewHolder>() {

    inner class UlasanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgUser: CircleImageView = itemView.findViewById(R.id.imgUser)
        val userNametxt: TextView = itemView.findViewById(R.id.userNametxt)
        val textDateAdd: TextView = itemView.findViewById(R.id.textDateAdd)
        val txtUlasan: TextView = itemView.findViewById(R.id.txtUlasan)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val ratingDouble: TextView = itemView.findViewById(R.id.ratingDouble)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UlasanViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.ulasan_items, parent, false)
        return UlasanViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UlasanViewHolder, position: Int) {
        val currentUlasan = ulasanList[position]

        Glide.with(holder.itemView)
            .load(currentUlasan.imageUser)
            .into(holder.imgUser)

        holder.userNametxt.text = currentUlasan.userName
        holder.textDateAdd.text = currentUlasan.dateAdded
        holder.txtUlasan.text = currentUlasan.ulasan
        holder.ratingBar.rating = currentUlasan.rating!!.toFloat()
        holder.ratingDouble.text = currentUlasan.rating.toString()
    }

    override fun getItemCount(): Int {
        return ulasanList.size
    }
}
