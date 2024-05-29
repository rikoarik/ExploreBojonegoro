package com.gracedian.explorebojonegoro.ui.dashboard.mytrips

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import com.gracedian.explorebojonegoro.utils.distancecalculate.calculateVincentyDistance

class WishlistViewModel : ViewModel() {

    private val _wishlistItems = MutableLiveData<List<WisataTerdekatItem>>()
    val wishlistItems: LiveData<List<WisataTerdekatItem>> get() = _wishlistItems

    private val _favoriteItems = MutableLiveData<List<WisataTerdekatItem>>()
    val favoriteItems: LiveData<List<WisataTerdekatItem>> get() = _favoriteItems

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("favorites")
    private val wisataReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("objekwisata")

    private var currentLocation: Location? = null

    fun setLocation(location: Location?) {
        currentLocation = location
    }

    fun retrieveWishlistItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            databaseReference.child(it).orderByValue().equalTo(true)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val items = mutableListOf<WisataTerdekatItem>()
                        for (data in snapshot.children) {
                            val itemId = data.key.toString()
                            getDataWisataTerdekat(itemId, items)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("WishlistViewModel", "Error: ${error.message}")
                    }
                })
        }
    }

    private fun getDataWisataTerdekat(wisata: String, items: MutableList<WisataTerdekatItem>) {
        val query = wisataReference.orderByChild("wisata").equalTo(wisata)
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
                        items.add(wisataTerdekatItem)
                        wisata?.let { setRatingTextByWisataName(it, items) }
                    }
                    items.sortBy { it.jarak }
                    _wishlistItems.postValue(items)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("WishlistViewModel", "Error: ${databaseError.message}")
            }
        })
    }

    private fun setRatingTextByWisataName(wisata: String, items: MutableList<WisataTerdekatItem>) {
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
                        }
                    }

                    val averageRating = if (totalReviews > 0) totalRating / totalReviews else 0.0
                    for (item in items) {
                        if (item.wisata == wisata) {
                            item.rating = averageRating
                        }
                    }
                    _wishlistItems.postValue(items)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("WishlistViewModel", "Database error: ${error.message}")
            }
        })
    }

    fun getFavoriteItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites").child(uid)
            favoritesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = mutableListOf<WisataTerdekatItem>()
                    for (childSnapshot in snapshot.children) {
                        val itemId = childSnapshot.key
                        val isFavorite = childSnapshot.getValue(Boolean::class.java) == true
                        for (item in _wishlistItems.value.orEmpty()) {
                            if (item.wisata == itemId) {
                                item.isFavorite = isFavorite
                                items.add(item)
                                break
                            }
                        }
                    }
                    _favoriteItems.postValue(items)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("WishlistViewModel", "Error: ${error.message}")
                }
            })
        }
    }
}
