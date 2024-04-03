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
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.Hotel
import com.gracedian.explorebojonegoro.utils.distancecalculate.calculateVincentyDistance
import kotlin.math.max

class PenginapanFragment : Fragment() {

    private lateinit var hotelAdapter: HotelAdapter
    private val hotelList = mutableListOf<Hotel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_penginapan, container, false)

        val rcPenginapan = view.findViewById<RecyclerView>(R.id.rcPenginapan)
        rcPenginapan.layoutManager = LinearLayoutManager(requireContext())
        hotelAdapter = HotelAdapter(hotelList) { hotel ->
            val intent = Intent(requireContext(), DetailsHotelActivity::class.java)
            intent.putExtra("nama_hotel", hotel.nama)
            startActivity(intent)
        }
        rcPenginapan.adapter = hotelAdapter

        fetchHotelData()

        return view
    }

    private fun fetchHotelData() {
        val wisataLat = arguments?.getDouble("latitude") ?: 0.0
        val wisataLong = arguments?.getDouble("longitude") ?: 0.0
        val databaseReference = FirebaseDatabase.getInstance().reference.child("Hotel")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val hotelName = snapshot.child("Hotel").getValue(String::class.java) ?: ""
                    val alamat = snapshot.child("alamat").getValue(String::class.java) ?: ""
                    val imageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
                    val latitude = snapshot.child("latitude").getValue(String::class.java) ?: ""
                    val longitude = snapshot.child("longitude").getValue(String::class.java) ?: ""

                    val jarak = calculateVincentyDistance(wisataLat, wisataLong, latitude.toDouble(), longitude.toDouble()) / 1000
                    val intValue = jarak.toInt()
                    val hotelTerdekatItem = Hotel(
                        hotelName,
                        alamat,
                        imageUrl,
                        latitude,
                        longitude,
                        rating = 0.0,
                        intValue
                    )
                    hotelList.add(hotelTerdekatItem)
                    setRatingTextByHotelName(hotelName)
                }
                hotelAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
    private fun setRatingTextByHotelName(hotel: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("UlasanHotel").child(hotel).addValueEventListener(object : ValueEventListener {
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
                    for (item in hotelList) {
                        if (item.nama == hotel) {
                            item.rating = averageRating
                        }
                    }

                    hotelAdapter.notifyDataSetChanged()


                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Handle error
            }
        })
    }

}
