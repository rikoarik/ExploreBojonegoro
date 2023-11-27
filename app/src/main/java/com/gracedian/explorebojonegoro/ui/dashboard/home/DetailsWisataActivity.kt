package com.gracedian.explorebojonegoro.ui.dashboard.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.DetailsPagerAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.GaleriFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.PenginapanFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.RestoranFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.TentangFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.UlasanFragment
import com.gracedian.explorebojonegoro.ui.dashboard.mytrips.adapter.MyTripsPagerAdapter

class DetailsWisataActivity : AppCompatActivity() {

    private lateinit var imgWisata: ImageView
    private lateinit var categorytxt: TextView
    private lateinit var reviewtxt: TextView
    private lateinit var namaWisata: TextView
    private lateinit var locWisata: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btBack: ImageView
    private lateinit var adapter: DetailsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_wisata)

        init()
        getData()
        btBack.setOnClickListener {
            finish()
        }

    }
    private fun init(){
        imgWisata = findViewById(R.id.imgWisata)
        categorytxt = findViewById(R.id.categorytxt)
        reviewtxt = findViewById(R.id.reviewtxt)
        namaWisata = findViewById(R.id.namaWisata)
        locWisata = findViewById(R.id.locWisata)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        btBack = findViewById(R.id.btBack)

        adapter = DetailsPagerAdapter(this)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tentang"
                1 -> "Galeri"
                2 -> "Penginapan"
                3 -> "Restoran"
                4 -> "Ulasan"
                else -> ""
            }
        }.attach()


    }
    private fun getData() {
        val receivedIntent = intent
        val nameWisata1 = receivedIntent.getStringExtra("wisata")
        val db = FirebaseDatabase.getInstance().getReference("datawisata")
        val query =  db.orderByChild("wisata").equalTo(nameWisata1)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val wisata = childSnapshot.child("wisata").getValue(String::class.java)
                        val alamat = childSnapshot.child("alamat").getValue(String::class.java)
                        val latString = childSnapshot.child("lat").getValue(String::class.java)
                        val longString = childSnapshot.child("long").getValue(String::class.java)

                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0

                        namaWisata.text = wisata
                        locWisata.text = alamat
                        viewPager.adapter = adapter
                        val tentangFragment = TentangFragment()
                        val bundle = Bundle()
                        bundle.putString("namaWisata", namaWisata.text.toString())
                        bundle.putDouble("lat", lat)
                        bundle.putDouble("long", long)
                        tentangFragment.arguments = bundle

                        val fragmentList = mutableListOf(tentangFragment, GaleriFragment(), PenginapanFragment(), RestoranFragment(), UlasanFragment())
                        adapter.setFragmentList(fragmentList)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }

}