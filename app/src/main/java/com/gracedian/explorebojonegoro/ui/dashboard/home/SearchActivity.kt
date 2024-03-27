package com.gracedian.explorebojonegoro.ui.dashboard.home

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.SearchAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.SearchItem
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class SearchActivity : AppCompatActivity(), SearchAdapter.OnItemClickListener{

    private lateinit var backButton: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var searchLayout: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var filterButton: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var notFoundImageView: ImageView

    private val searchItemsList =  mutableListOf<SearchItem>()
    private val searchAdapter = SearchAdapter(searchItemsList, this)

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("objekwisata")

    private var appliedCategory: String = ""
    private var appliedRating: Float = 0.0f
    private var appliedJarakMax: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val (savedCategory, savedRating, savedJarakMax) = SharedPrefManager.getFilterPreferences(this)
        appliedCategory = savedCategory
        appliedRating = savedRating
        appliedJarakMax = savedJarakMax

        initializeViews()
        setupListeners()

        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        searchRecyclerView.adapter = searchAdapter
        getData()

    }

    private fun initializeViews() {
        backButton = findViewById(R.id.btBack)
        titleTextView = findViewById(R.id.textView6)
        searchLayout = findViewById(R.id.linearLayout6)
        searchEditText = findViewById(R.id.editTextSearch)
        filterButton = findViewById(R.id.btFilter)
        resultTextView = findViewById(R.id.hasil)
        searchRecyclerView = findViewById(R.id.rcSearch)
        notFoundImageView = findViewById(R.id.notFound)
    }

    private fun setupListeners() {
        backButton.setOnClickListener {
            SharedPrefManager.saveFilterPreferences(this, "Pilih Kategori", 0.0F, 0)
            finish()
        }

        filterButton.setOnClickListener {
            showFilterDialog()
        }
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No action needed
            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                filterSearchResults(query)
                if (query.isEmpty()){
                    notFoundImageView.visibility = View.GONE
                    getData()
                }
            }
        })
    }

    private fun showFilterDialog() {
        val fragmentManager = supportFragmentManager
        val filterDialog = FilterBottomSheetFragment()
        filterDialog.onFilterApplied = { category, rating, jarakMax ->
            appliedCategory = category
            appliedRating = rating
            appliedJarakMax = jarakMax
            SharedPrefManager.saveFilterPreferences(this, appliedCategory, appliedRating, appliedJarakMax)
            getData()
        }
        filterDialog.onFilterReset = {
            appliedCategory = "Pilih Kategori"
            appliedRating = 0.0f
            appliedJarakMax = 0
            SharedPrefManager.saveFilterPreferences(this, appliedCategory, appliedRating, appliedJarakMax)
            getData()
        }
        filterDialog.show(fragmentManager, "FilterFragment")
    }

    private fun getData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val previousFavorites = mutableMapOf<String, Boolean>() // Store previous favorite status
                for (item in searchItemsList) {
                    previousFavorites[item.wisata ?: ""] = item.isFavorite
                }
                if (dataSnapshot.exists()) {
                    searchItemsList.clear()
                    for (childSnapshot in dataSnapshot.children) {
                        val imageUrl = childSnapshot.child("imageUrl").getValue(String::class.java)
                        val title = childSnapshot.child("wisata").getValue(String::class.java) ?: ""
                        val location = childSnapshot.child("alamat").getValue(String::class.java) ?: ""
                        val ratingString = childSnapshot.child("rating").getValue(Float::class.java)
                        val latString = childSnapshot.child("latitude").getValue(String::class.java)
                        val longString = childSnapshot.child("longitude").getValue(String::class.java)
                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0

                        val rating = ratingString ?: 0.0

                        val receivedIntent = intent
                        val latitude = receivedIntent.getDoubleExtra("lat", 0.0)
                        val longitude = receivedIntent.getDoubleExtra("long", 0.0)
                        val jarak = calculateVincentyDistance(latitude, longitude, lat, long) / 1000
                        val intValue = jarak.toInt()

                        val category = childSnapshot.child("kategori").getValue(String::class.java) ?: ""

                        val searchItem = SearchItem(
                            imageUrl = imageUrl,
                            wisata = title,
                            alamat = location,
                            rating = 3.0,
                            jarak = intValue
                        )
                        if (isRatingMatch(searchItem) && isJarakMaxMatch(searchItem)) {
                            searchItemsList.add(searchItem)
                        }
                    }
                    searchAdapter.notifyDataSetChanged()
                    getFavoriteItems()
                    resultTextView.text = "${searchItemsList.size} hasil ditemukan"

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }
    private fun filterSearchResults(query: String) {
        val filteredItems = searchItemsList.filter { searchItem ->
            val isRatingMatch = isRatingMatch(searchItem)
            val isJarakMaxMatch = isJarakMaxMatch(searchItem)
            val isQueryMatch = isQueryMatch(searchItem, query)

            isRatingMatch && isJarakMaxMatch && isQueryMatch
        }

        updateFilteredItemsView(filteredItems)

    }

    private fun isRatingMatch(searchItem: SearchItem): Boolean {
        return appliedRating == 0.0f || (searchItem.rating ?: 0.0) >= appliedRating
    }

    private fun isJarakMaxMatch(searchItem: SearchItem): Boolean {
        return appliedJarakMax == 0 || searchItem.jarak!! <= appliedJarakMax
    }

    private fun isQueryMatch(searchItem: SearchItem, query: String): Boolean {
        return searchItem.wisata?.contains(query, ignoreCase = true) == true
    }

    private fun updateFilteredItemsView(filteredItems: List<SearchItem>) {
        if (filteredItems.isNotEmpty()) {
            notFoundImageView.visibility = View.GONE
            searchAdapter.setItems(filteredItems)
            resultTextView.text = "${filteredItems.size} hasil ditemukan"
        } else {
            notFoundImageView.visibility = View.VISIBLE
            searchAdapter.setItems(emptyList())
            resultTextView.text = "0 hasil ditemukan"
        }
    }


    private fun calculateVincentyDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val a = 6378137.0 // Radius semi-mayor Bumi dalam meter
        val b = 6356752.3142 // Radius semi-minor Bumi dalam meter
        val f = 1 / 298.257223563 // Flattening

        val phi1 = Math.toRadians(lat1)
        val lambda1 = Math.toRadians(lon1)
        val phi2 = Math.toRadians(lat2)
        val lambda2 = Math.toRadians(lon2)

        val L = lambda2 - lambda1

        var tanU1 = (1 - f) * tan(phi1)
        val cosU1 = 1 / sqrt(1 + tanU1 * tanU1)
        val sinU1 = tanU1 * cosU1

        var tanU2 = (1 - f) * tan(phi2)
        val cosU2 = 1 / sqrt(1 + tanU2 * tanU2)
        val sinU2 = tanU2 * cosU2

        var lambda = L
        var lambdaP: Double
        var iterLimit = 100
        var cosAlpha: Double
        var sinSigma: Double
        var cosSigma: Double
        var sigma: Double
        var cos2SigmaM: Double
        var cosSigmaM: Double

        do {
            val sinLambda = sin(lambda)
            val cosLambda = cos(lambda)

            sinSigma = sqrt((cosU2 * sinLambda).pow(2) + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda).pow(2))
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
            sigma = atan2(sinSigma, cosSigma)

            val sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
            cosAlpha = 1 - sinAlpha * sinAlpha

            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosAlpha
            val C = f / 16 * cosAlpha * (4 + f * (4 - 3 * cosAlpha))
            lambdaP = lambda
            lambda = L + (1 - C) * f * sinAlpha * (sigma + C * sinAlpha * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM.pow(2))))
        } while (abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0)

        if (iterLimit == 0) {
            throw IllegalStateException("Vincenty formula failed to converge")
        }

        val u2 = cosAlpha * (a * a - b * b) / (b * b)
        val A = 1 + u2 / 16384 * (4096 + u2 * (-768 + u2 * (320 - 175 * u2)))
        val B = u2 / 1024 * (256 + u2 * (-128 + u2 * (74 - 47 * u2)))
        val deltaSigma = B * sinSigma * (cos2SigmaM + B / 4 * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM * (-3 + 4 * sinSigma.pow(2)) * (-3 + 4 * cos2SigmaM * cos2SigmaM)))

        val s = b * A * (sigma - deltaSigma)

        return s
    }

    override fun onBackPressed() {
        super.onBackPressed()
        SharedPrefManager.saveFilterPreferences(this, "Pilih Kategori", 0.0F, 0)
        finish()
    }

    override fun onItemClick(position: Int) {
        val searchItem = searchItemsList[position]
        val intent = Intent(this, DetailsWisataActivity::class.java)
        intent.putExtra("wisata", searchItem.wisata)
        startActivity(intent)
    }

    override fun onFavoriteClick(position: Int) {
        val searchItem = searchItemsList[position]
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites").child(userId)

            val newFavoriteStatus = !searchItem.isFavorite

            if (newFavoriteStatus) {
                searchItem.wisata?.let {
                    favoritesRef.child(it).setValue(true)
                        .addOnSuccessListener {
                            val imageView = searchRecyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
                            imageView?.setImageResource(R.drawable.ic_favorite_true)
                            showToast("Item added to favorites")
                        }
                        .addOnFailureListener { e ->
                            showToast("Failed to add item to favorites: ${e.message}")
                        }
                }
            } else {
                searchItem.wisata?.let {
                    favoritesRef.child(it).setValue(false)
                        .addOnSuccessListener {
                            val imageView = searchRecyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
                            imageView?.setImageResource(R.drawable.ic_favorite_false)
                            showToast("Item removed from favorites")
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

                        for (item in searchItemsList) {
                            if (item.wisata == itemId) {
                                item.isFavorite = isFavorite ?: false
                                val position = searchItemsList.indexOf(item)
                                val imageView = searchRecyclerView.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)

                                if (isFavorite == true){
                                    imageView?.setImageResource(R.drawable.ic_favorite_true)
                                } else {
                                    imageView?.setImageResource(R.drawable.ic_favorite_false)
                                }
                                break
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("getFavoriteItems", "Error: ${error.message}")
                }
            })
        }
    }
    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}