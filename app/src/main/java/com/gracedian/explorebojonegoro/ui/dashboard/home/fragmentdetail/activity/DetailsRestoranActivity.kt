package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter.RestoranDetailsPagerAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran.GalleryRestoranFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran.TentangRestoranFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran.UlasanRestoranFragment

class DetailsRestoranActivity : AppCompatActivity() {

    private lateinit var btBack: ImageView
    private lateinit var imgRestoran: ImageView
    private lateinit var reviewtxt: TextView
    private lateinit var namaRestoran: TextView
    private lateinit var locRestoran: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btNavigasi: AppCompatButton
    private lateinit var adapter: RestoranDetailsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_restoran)

        init()
        fetchRestoranData()
        btBack.setOnClickListener {
            finish()
        }
    }
    private fun init(){
        btBack = findViewById(R.id.btBack)
        imgRestoran = findViewById(R.id.imgRestoran)
        reviewtxt = findViewById(R.id.reviewtxt)
        namaRestoran = findViewById(R.id.namaRestoran)
        locRestoran = findViewById(R.id.locRestoran)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        btNavigasi = findViewById(R.id.btNavigasi)
        adapter = RestoranDetailsPagerAdapter(this)
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
    private fun fetchRestoranData() {
        val restoran = intent.getStringExtra("nama_restoran").toString()
        val databaseReference = FirebaseDatabase.getInstance().reference.child("Restoran").orderByChild(restoran)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    for (snapshot in dataSnapshot.children) {
                        val restoranName = snapshot.child("Restoran").getValue(String::class.java) ?: ""
                        val alamat = snapshot.child("alamat").getValue(String::class.java) ?: ""
                        val imageUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
                        val latitude = snapshot.child("latitude").getValue(String::class.java) ?: ""
                        val longitude = snapshot.child("longitude").getValue(String::class.java) ?: ""

                        val lat = latitude.toDouble()
                        val long = longitude.toDouble()
                        namaRestoran.text = restoranName
                        locRestoran.text = alamat
                        Glide.with(this@DetailsRestoranActivity)
                            .load(imageUrl)
                            .into(imgRestoran)
                        val bundle = Bundle().apply {
                            putString("namaRestoran", restoranName)
                            putString("alamat", alamat)
                            putDouble("latitude", lat)
                            putDouble("longitude", long)
                        }
                        adapter.setData(bundle)

                        val fragmentList = mutableListOf(
                            TentangRestoranFragment(),
                            GalleryRestoranFragment(),
                            UlasanRestoranFragment()
                        )
                        adapter.setFragmentList(fragmentList)
                        setRatingTextByWisataName(restoran)

                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
    private fun setRatingTextByWisataName(restoran: String) {
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