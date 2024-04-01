package com.gracedian.explorebojonegoro.ui.dashboard.home

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
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

class DetailsWisataActivity : AppCompatActivity() {

    private lateinit var imgWisata: ImageView
    private lateinit var categorytxt: TextView
    private lateinit var reviewtxt: TextView
    private lateinit var namaWisata: TextView
    private lateinit var locWisata: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var btBack: ImageView
    private lateinit var btFavorite: ImageButton
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

                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0

                        namaWisata.text = wisata
                        locWisata.text = alamat
                        categorytxt.text = kategori
                        Glide.with(this@DetailsWisataActivity)
                            .load(imageUrl)
                            .into(imgWisata)

                        // Mengatur bundle untuk dikirim ke adapter
                        val bundle = Bundle().apply {
                            putString("namaWisata", wisata)
                            putString("alamat", alamat)
                            putDouble("latitude", lat)
                            putDouble("longitude", long)
                        }
                        adapter.setData(bundle)

                        // Mengatur fragment-list untuk dikirim ke adapter
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

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


}
