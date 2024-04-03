package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter.HotelDetailsPagerAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailshotel.GalleryHotelFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailshotel.TentangHotelFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailshotel.UlasanHotelFragment

class DetailsHotelActivity : AppCompatActivity() {
    
    private lateinit var btBack: ImageView
    private lateinit var imgHotel: ImageView
    private lateinit var reviewtxt: TextView
    private lateinit var namaHotel: TextView
    private lateinit var locHotel: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btNavigasi: AppCompatButton
    private lateinit var adapter: HotelDetailsPagerAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_hotel)

        init()
        fetchHotelData()
        btBack.setOnClickListener {
            finish()
        }
    }
    private fun init(){
        btBack = findViewById(R.id.btBack)
        imgHotel = findViewById(R.id.imgHotel)
        reviewtxt = findViewById(R.id.reviewtxt)
        namaHotel = findViewById(R.id.namaHotel)
        locHotel = findViewById(R.id.locHotel)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        btNavigasi = findViewById(R.id.btNavigasi)
        adapter = HotelDetailsPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tentang"
                1 -> "Galeri"
                2 -> "Ulasan"
                else -> ""
            }
        }.attach()
    }
    private fun fetchHotelData() {
        val Hotel = intent.getStringExtra("nama_hotel").toString()
        val databaseReference = FirebaseDatabase.getInstance().reference.child("Hotel").orderByChild(Hotel)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    for (snapshot in dataSnapshot.children) {
                        val HotelName = snapshot.child("Hotel").getValue(String::class.java) ?: ""
                        val alamat = snapshot.child("alamat").getValue(String::class.java) ?: ""
                        val imageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
                        val latitude = snapshot.child("latitude").getValue(String::class.java) ?: ""
                        val longitude = snapshot.child("longitude").getValue(String::class.java) ?: ""

                        val lat = latitude.toDouble()
                        val long = longitude.toDouble()
                        namaHotel.text = HotelName
                        locHotel.text = alamat
                        Glide.with(this@DetailsHotelActivity)
                            .load(imageUrl)
                            .into(imgHotel)
                        val bundle = Bundle().apply {
                            putString("namaHotel", HotelName)
                            putString("alamat", alamat)
                            putDouble("latitude", lat)
                            putDouble("longitude", long)
                        }
                        adapter.setData(bundle)

                        val fragmentList = mutableListOf(
                            TentangHotelFragment(),
                            GalleryHotelFragment(),
                            UlasanHotelFragment()
                        )
                        adapter.setFragmentList(fragmentList)
                        setRatingTextByWisataName(Hotel)

                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
    private fun setRatingTextByWisataName(Hotel: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("UlasanHotel").child(Hotel).addValueEventListener(object :
            ValueEventListener {
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

                    val averageRating = if (totalReviews > 0) totalRating / totalReviews else 0.0
                    val ratingText = String.format("$averageRating ($totalReviews Reviews)")
                    reviewtxt.text = ratingText
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Handle error
            }
        })
    }

}