package com.gracedian.explorebojonegoro.ui.dashboard.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.activity.DetailsWisataActivity
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.WisataTerdekatAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import com.gracedian.explorebojonegoro.utils.distancecalculate.calculateVincentyDistance
import kotlin.math.max

class WishListActivity : AppCompatActivity(), WisataTerdekatAdapter.OnItemClickListener {

    private lateinit var btBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WisataTerdekatAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val wishlistItems = mutableListOf<WisataTerdekatItem>()
    private var currentLocation: Location? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var editTextSearch: EditText
    private val filteredWishlistItems = mutableListOf<WisataTerdekatItem>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_list)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        editTextSearch = findViewById(R.id.editTextSearch)
        recyclerView = findViewById(R.id.rcWishlist)
        btBack = findViewById(R.id.btBack)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WisataTerdekatAdapter(filteredWishlistItems, this)
        recyclerView.adapter = adapter

        databaseReference = FirebaseDatabase.getInstance().getReference("favorites")

        btBack.setOnClickListener {
            finish()
        }

        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterWishlistItems(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        getLocation()
        retrieveWishlistItems()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = it
                }
            }
    }

    private fun retrieveWishlistItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            databaseReference.child(userId).orderByValue().equalTo(true)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        wishlistItems.clear()
                        for (data in snapshot.children) {
                            val itemId = data.key.toString()
                            getDataWisataTerdekat(itemId)
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // Handle onCancelled event
                    }
                })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterWishlistItems(query: String) {
        filteredWishlistItems.clear()
        if (query.isEmpty()) {
            filteredWishlistItems.addAll(wishlistItems)
        } else {
            filteredWishlistItems.addAll(wishlistItems.filter { item ->
                item.wisata?.contains(query, ignoreCase = true) == true || item.alamat?.contains(query, ignoreCase = true) == true
            })
        }
        adapter.notifyDataSetChanged()
    }

    private fun getDataWisataTerdekat(wisata: String) {
        val db = FirebaseDatabase.getInstance().getReference("objekwisata")
        val query = db.orderByChild("wisata").equalTo(wisata)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val wisata = childSnapshot.child("wisata").getValue(String::class.java)
                        val alamat = childSnapshot.child("alamat").getValue(String::class.java)
                        val latString = childSnapshot.child("latitude").getValue(String::class.java)
                        val longString = childSnapshot.child("longitude").getValue(String::class.java)
                        val imageUrl = childSnapshot.child("imageUrl").getValue(String::class.java)

                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0

                        val jarak = calculateVincentyDistance(currentLocation?.latitude ?: 0.0, currentLocation?.longitude ?: 0.0, lat, long) / 1000

                        val intValue = jarak.toInt()

                        val wisataTerdekatItem = WisataTerdekatItem(
                            imageUrl = imageUrl,
                            wisata = wisata,
                            rating = 0.0,
                            alamat = alamat,
                            jarak = intValue,
                            lat = lat,
                            long = long,
                        )
                        wishlistItems.add(wisataTerdekatItem)

                        if (wisata != null) {
                            setRatingTextByWisataName(wisata)
                        }
                    }
                    wishlistItems.sortBy { it.jarak }
                    filterWishlistItems(editTextSearch.text.toString().trim())
                    getFavoriteItems()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }

    override fun onItemTerdekatClick(position: Int) {
        val terdekatItem = filteredWishlistItems[position]
        val intent = Intent(this, DetailsWisataActivity::class.java)
        intent.putExtra("wisata", terdekatItem.wisata)
        startActivity(intent)
    }

    override fun onFavoriteClick(position: Int) {
        val favoriteItem = filteredWishlistItems[position]
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites").child(userId)
            val newFavoriteStatus = !favoriteItem.isFavorite

            if (newFavoriteStatus) {
                favoriteItem.wisata?.let {
                    favoritesRef.child(it).setValue(true)
                        .addOnSuccessListener {
                            val imageView = recyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
                            imageView?.setImageResource(R.drawable.ic_favorite_true)
                            showToast("Wisata ditambahkan ke wishlist")
                        }
                        .addOnFailureListener { e ->
                            showToast("Gagal untuk menambahkan wisata ke wishlist: ${e.message}")
                        }
                }
            } else {
                favoriteItem.wisata?.let {
                    favoritesRef.child(it).setValue(false)
                        .addOnSuccessListener {
                            val imageView = recyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
                            imageView?.setImageResource(R.drawable.ic_favorite_false)
                            showToast("Wisata dihapus dari wishlist")
                        }
                        .addOnFailureListener { e ->
                            showToast("Failed to remove item from favorites: ${e.message}")
                        }
                }
            }
        }
    }

    private fun getFavoriteItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites").child(uid)
            favoritesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val itemId = childSnapshot.key
                        val isFavorite = childSnapshot.getValue(Boolean::class.java)
                        for (item in wishlistItems) {
                            if (item.wisata == itemId) {
                                item.isFavorite = isFavorite ?: false
                                val position = filteredWishlistItems.indexOf(item)
                                val imageView = recyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
                                if (isFavorite == true) {
                                    imageView?.setImageResource(R.drawable.ic_favorite_true)
                                }

                                break
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("getFavoriteItems", "Error: ${error.message}")
                }
            })
        }
    }

    private fun setRatingTextByWisataName(wisata: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("Ulasan").child(wisata).addValueEventListener(object :
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

                    totalReviews = max(totalReviews, 1)

                    val averageRating = totalRating / totalReviews
                    for (item in wishlistItems) {
                        if (item.wisata == wisata) {
                            item.rating = averageRating
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Handle error
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
