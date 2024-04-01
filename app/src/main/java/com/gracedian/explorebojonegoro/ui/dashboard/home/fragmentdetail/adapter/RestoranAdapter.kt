package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.Hotel
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.Restoran

class RestoranAdapter(private val restoranList: List<Restoran>) : RecyclerView.Adapter<RestoranAdapter.RestoranViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestoranViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.terdekat_items, parent, false)
        return RestoranViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestoranViewHolder, position: Int) {
        val restoran = restoranList[position]
        holder.bind(restoran)
    }

    override fun getItemCount(): Int {
        return restoranList.size
    }

    class RestoranViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(restoran: Restoran) {

            itemView.findViewById<TextView>(R.id.namaPenginapan).text = restoran.nama
            itemView.findViewById<TextView>(R.id.locPenginapan).text = restoran.alamat


            Glide.with(itemView.context)
                .load(restoran.imageUrl)
                .into(itemView.findViewById(R.id.imgPenginapan))
            itemView.findViewById<TextView>(R.id.txtjarak).text = " ${restoran.jarak} Km"
        }
    }
}

