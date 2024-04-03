package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter.HotelAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter.RestoranAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.Hotel
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.Restoran
import com.gracedian.explorebojonegoro.utils.distancecalculate.calculateVincentyDistance
import kotlin.math.max

class RestoranFragment : Fragment() {

    private lateinit var restoranAdapter: RestoranAdapter
    private val restoranList = mutableListOf<Restoran>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_restoran, container, false)

        val rcPenginapan = view.findViewById<RecyclerView>(R.id.rcRestoran)
        rcPenginapan.layoutManager = LinearLayoutManager(requireContext())
        restoranAdapter = RestoranAdapter(restoranList) { restoran ->
            val intent = Intent(requireContext(), DetailsRestoranActivity::class.java)
            intent.putExtra("nama_restoran", restoran.nama)
            startActivity(intent)
        }
        rcPenginapan.adapter = restoranAdapter

        fetchHotelData()

        return view
    }

    private fun fetchHotelData() {
        val restoranLat = arguments?.getDouble("latitude") ?: 0.0
        val restoranLong = arguments?.getDouble("longitude") ?: 0.0
        val databaseReference = FirebaseDatabase.getInstance().reference.child("Restoran")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val restoranName = snapshot.child("Restoran").getValue(String::class.java) ?: ""
                    val alamat = snapshot.child("alamat").getValue(String::class.java) ?: ""
                    val imageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
                    val latitude = snapshot.child("latitude").getValue(String::class.java) ?: ""
                    val longitude = snapshot.child("longitude").getValue(String::class.java) ?: ""

                    val jarak = calculateVincentyDistance(restoranLat, restoranLong, latitude.toDouble(), longitude.toDouble()) / 1000
                    val intValue = jarak.toInt()
                    val restoranTerdekatItem = Restoran(
                        restoranName,
                        alamat,
                        imageUrl,
                        latitude,
                        longitude,
                        rating = 0.0,
                        intValue
                    )
                    restoranList.add(restoranTerdekatItem)
                    setRatingTextByRestoranName(restoranName)
                }
                restoranAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
    private fun setRatingTextByRestoranName(restoran: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("UlasanRestoran").child(restoran).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalRating = 0.0
                var totalReviews = 0
                if (snapshot.exists()) {
                    for (ulasanSnapshot in snapshot.children) {
                        val rating = ulasanSnapshot.child("rating").getValue(Double::class.java)
                        rating?.let {
                            totalRating += it
                            totalReviews++
                        } ?: run {
                            Log.e("Rating", "Null value found for rating in ulasan: ${ulasanSnapshot.key}")
                        }
                    }

                    totalReviews = max(totalReviews, 1)

                    val averageRating = totalRating / totalReviews
                    for (item in restoranList) {
                        if (item.nama == restoran) {
                            item.rating = averageRating
                        }
                    }

                    restoranAdapter.notifyDataSetChanged()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Handle error
            }
        })
    }

}