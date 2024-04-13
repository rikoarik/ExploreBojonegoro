package com.gracedian.explorebojonegoro.ui.dashboard.home.activity

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
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
import com.gracedian.explorebojonegoro.ui.navigateroute.RouteNavigateActivity

class DetailsWisataActivity : AppCompatActivity() {

    private lateinit var imgWisata: ImageView
    private lateinit var categorytxt: TextView
    private lateinit var reviewtxt: TextView
    private lateinit var namaWisata: TextView
    private lateinit var locWisata: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btBack: ImageView
    private lateinit var btNavigasi: AppCompatButton
    private lateinit var btFavorite: ImageButton
    private lateinit var adapter: DetailsPagerAdapter
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_wisata)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        init()
        getData()
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLat = it.latitude
                    val currentLong = it.longitude

                    // Simpan koordinat ini ke intent sebelum memulai RouteNavigateActivity
                    btNavigasi.setOnClickListener {
                        val intent = Intent(this, RouteNavigateActivity::class.java)
                        intent.putExtra("latDestination", lat)
                        intent.putExtra("longDestination", long)
                        intent.putExtra("latOrigin", currentLat)
                        intent.putExtra("longOrigin", currentLong)
                        startActivity(intent)
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
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
        btNavigasi = findViewById(R.id.btNavigasi)
        btFavorite = findViewById(R.id.btFavorite)

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
        btFavorite.setOnClickListener {
            favoriteClick()
        }

    }
    private fun getData() {
        val receivedIntent = intent
        val nameWisata = receivedIntent.getStringExtra("wisata")
        val db = FirebaseDatabase.getInstance().getReference("objekwisata")
        val query =  db.orderByChild("wisata").equalTo(nameWisata)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val wisata = childSnapshot.child("wisata").getValue(String::class.java)
                        val alamat = childSnapshot.child("alamat").getValue(String::class.java)
                        val latString = childSnapshot.child("latitude").getValue(String::class.java)
                        val longString = childSnapshot.child("longitude").getValue(String::class.java)
                        val imageUrl = childSnapshot.child("imageUrl").getValue(String::class.java)
                        val kategori = childSnapshot.child("kategori").getValue(String::class.java)

                        lat = latString?.toDoubleOrNull() ?: 0.0
                        long = longString?.toDoubleOrNull() ?: 0.0
                        if (wisata != null) {
                            setRatingTextByWisataName(wisata)
                        }
                        namaWisata.text = wisata
                        locWisata.text = alamat
                        categorytxt.text = kategori
                        Glide.with(this@DetailsWisataActivity)
                            .load(imageUrl)
                            .into(imgWisata)

                        val bundle = Bundle().apply {
                            putString("namaWisata", wisata)
                            putString("alamat", alamat)
                            putDouble("latitude", lat)
                            putDouble("longitude", long)
                        }
                        adapter.setData(bundle)

                        val fragmentList = mutableListOf(
                            TentangFragment(),
                            GaleriFragment(),
                            PenginapanFragment(),
                            RestoranFragment(),
                            UlasanFragment()
                        )
                        getFavoriteItems()
                        adapter.setFragmentList(fragmentList)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }
    private fun favoriteClick() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val wisata = namaWisata.text.toString().trim()
        if (userId != null) {
            val favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites").child(userId)

            favoritesRef.child(wisata).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val currentFavoriteStatus = snapshot.getValue(Boolean::class.java) ?: false

                        val newFavoriteStatus = !currentFavoriteStatus

                        favoritesRef.child(wisata).setValue(newFavoriteStatus)
                            .addOnSuccessListener {
                                updateFavoriteUI(newFavoriteStatus)
                                val successMessage = if (newFavoriteStatus) "Wisata ditambahkan ke wishlist" else "Wisata dihapus dari wishlist"
                                showToast(this@DetailsWisataActivity, successMessage)
                            }
                            .addOnFailureListener { e ->
                                val failureMessage = "Failed to ${if (newFavoriteStatus) "add" else "remove"} item from favorites: ${e.message}"
                                showToast(this@DetailsWisataActivity, failureMessage)
                            }
                    } else {
                        val newFavoriteStatus = true
                        favoritesRef.child(wisata).setValue(newFavoriteStatus)
                            .addOnSuccessListener {
                                updateFavoriteUI(newFavoriteStatus)
                                val successMessage = "Wisata ditambahkan ke wishlistWisata ditambahkan ke wishlist"
                                showToast(this@DetailsWisataActivity, successMessage)
                            }
                            .addOnFailureListener { e ->
                                val failureMessage = "Failed to add item to favorites: ${e.message}"
                                showToast(this@DetailsWisataActivity, failureMessage)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    val errorMessage = "Failed to retrieve favorite status: ${error.message}"
                    showToast(this@DetailsWisataActivity, errorMessage)
                }
            })
        }
    }


    private fun getFavoriteItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val receivedIntent = intent
        val itemId = receivedIntent.getStringExtra("wisata").toString()
        userId?.let { uid ->
            val favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites").child(uid)
            val query = favoritesRef.child(itemId)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isFavorite = snapshot.getValue(Boolean::class.java)

                    // Update UI based on the favorite status
                    if (isFavorite == true) {
                        btFavorite.setImageResource(R.drawable.ic_favorite_true)
                    } else {
                        btFavorite.setImageResource(R.drawable.ic_favorite_false)
                    }
                    Log.d("Favorite Item", "$itemId isFavorite: $isFavorite")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("getFavoriteItems", "Error: ${error.message}")
                }
            })
        }
    }

    private fun updateFavoriteUI(isFavorite: Boolean) {
        val drawableResId = if (isFavorite) R.drawable.ic_favorite_true else R.drawable.ic_favorite_false
        btFavorite.setImageResource(drawableResId)
    }

    private fun setRatingTextByWisataName(wisata: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("Ulasan").child(wisata).addValueEventListener(object : ValueEventListener {
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



    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


}
