package com.gracedian.explorebojonegoro.ui.dashboard.home.activity

import android.annotation.SuppressLint
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
import com.gracedian.explorebojonegoro.ui.dashboard.home.FilterBottomSheetFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.SearchAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.SearchItem
import com.gracedian.explorebojonegoro.utils.distancecalculate.calculateVincentyDistance
import kotlin.math.max

class SearchActivity : AppCompatActivity(), SearchAdapter.OnItemClickListener {

    lateinit var backButton: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var searchLayout: LinearLayout
    lateinit var searchEditText: EditText
    lateinit var filterButton: ImageView
    lateinit var resultTextView: TextView
    lateinit var searchRecyclerView: RecyclerView
    lateinit var notFoundImageView: ImageView

    val searchItemsList = mutableListOf<SearchItem>()
    var searchAdapter = SearchAdapter(searchItemsList, this)

    var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("objekwisata")

    var appliedCategory: String = ""
    var appliedRating: Float = 0.0f
    var appliedJarakMax: Int = 0

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

    fun initializeViews() {
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
                if (query.isEmpty()) {
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
            @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val previousFavorites = mutableMapOf<String, Boolean>() // Store previous favorite status
                for (item in searchItemsList) {
                    previousFavorites[item.wisata ?: ""] = item.isFavorite
                }
                if (dataSnapshot.exists()) {
                    searchItemsList.clear()
                    val dataItems = mutableListOf<SearchItem>()
                    for (childSnapshot in dataSnapshot.children) {
                        val imageUrl = childSnapshot.child("imageUrl").getValue(String::class.java)
                        val title = childSnapshot.child("wisata").getValue(String::class.java) ?: ""
                        val location = childSnapshot.child("alamat").getValue(String::class.java) ?: ""
                        val latString = childSnapshot.child("latitude").getValue(String::class.java)
                        val longString = childSnapshot.child("longitude").getValue(String::class.java)
                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0

                        val receivedIntent = intent
                        val latitude = receivedIntent.getDoubleExtra("lat", 0.0)
                        val longitude = receivedIntent.getDoubleExtra("long", 0.0)
                        val jarak = calculateVincentyDistance(latitude, longitude, lat, long) / 1000
                        val intValue = jarak.toInt()

                        val category = childSnapshot.child("kategori").getValue(String::class.java) ?: ""

                        val searchItem = SearchItem(
                            imageUrl = imageUrl,
                            wisata = title,
                            kategori = category,
                            alamat = location,
                            rating = 0.0,
                            jarak = intValue
                        )
                        dataItems.add(searchItem)
                    }
                    updateSearchItemsWithRating(dataItems)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }

    private fun updateSearchItemsWithRating(dataItems: List<SearchItem>) {
        val updatedItems = mutableListOf<SearchItem>()
        val totalItems = dataItems.size
        var processedItems = 0

        dataItems.forEach { item ->
            setRatingTextByWisataName(item.wisata ?: "") { averageRating ->
                item.rating = averageRating
                if (isCategoryMatch(item) && isRatingMatch(item) && isJarakMaxMatch(item)) {
                    updatedItems.add(item)
                }
                processedItems++
                if (processedItems == totalItems) {
                    searchItemsList.clear()
                    searchItemsList.addAll(updatedItems)
                    searchAdapter.notifyDataSetChanged()
                    getFavoriteItems()

                    if (searchItemsList.isEmpty()) {
                        notFoundImageView.visibility = View.VISIBLE
                        resultTextView.text = "Maaf, Tidak Dapat Menemukan Hasil Pencarian Anda"
                    } else {
                        notFoundImageView.visibility = View.GONE
                        resultTextView.text = "${searchItemsList.size} hasil ditemukan"
                    }
                }
            }
        }
    }

    private fun filterSearchResults(query: String) {
        val filteredItems = searchItemsList.filter { searchItem ->
            val isQueryMatch = isQueryMatch(searchItem, query)
            val isCategoryMatch = isCategoryMatch(searchItem)
            val isRatingMatch = isRatingMatch(searchItem)
            val isJarakMaxMatch = isJarakMaxMatch(searchItem)

            isQueryMatch && isCategoryMatch && isRatingMatch && isJarakMaxMatch
        }

        updateFilteredItemsView(filteredItems)
    }

    private fun isRatingMatch(searchItem: SearchItem): Boolean {
        return appliedRating == 0.0f || (searchItem.rating ?: 0.0) >= appliedRating
    }

    private fun isJarakMaxMatch(searchItem: SearchItem): Boolean {
        return appliedJarakMax == 0 || searchItem.jarak!! <= appliedJarakMax
    }

    private fun isCategoryMatch(searchItem: SearchItem): Boolean {
        return appliedCategory == "" || searchItem.kategori == appliedCategory
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
            resultTextView.text = "Maaf, Tidak Dapat Menemukan Hasil Pencarian Anda"
        }
    }

    private fun setRatingTextByWisataName(wisata: String, callback: (Double) -> Unit) {
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

                    totalReviews = max(totalReviews, 1)
                    val averageRating = totalRating / totalReviews
                    callback(averageRating)
                } else {
                    callback(0.0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                callback(0.0)
            }
        })
    }

    private fun getFavoriteItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Favorit").child(userId)
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (favoriteSnapshot in dataSnapshot.children) {
                        val favoriteItem = favoriteSnapshot.getValue(SearchItem::class.java)
                        if (favoriteItem != null) {
                            for (searchItem in searchItemsList) {
                                if (searchItem.wisata == favoriteItem.wisata) {
                                    searchItem.isFavorite = true
                                    break
                                }
                            }
                        }
                    }
                    searchAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Firebase", "Database error: ${databaseError.message}")
                }
            })
        }
    }

    override fun onItemClick(position: Int) {
        val item = searchItemsList[position]
        val intent = Intent(this, DetailsWisataActivity::class.java)
        intent.putExtra("nama", item.wisata)
        startActivity(intent)
    }

    override fun onFavoriteClick(position: Int) {
        val item = searchItemsList[position]
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Favorit").child(userId)
            if (item.isFavorite) {
                // Remove from favorites
                databaseReference.child(item.wisata ?: "").removeValue()
                    .addOnSuccessListener {
                        item.isFavorite = false
                        searchAdapter.notifyItemChanged(position)
                        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Error removing favorite: ${e.message}")
                        Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Add to favorites
                databaseReference.child(item.wisata ?: "").setValue(item)
                    .addOnSuccessListener {
                        item.isFavorite = true
                        searchAdapter.notifyItemChanged(position)
                        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Error adding favorite: ${e.message}")
                        Toast.makeText(this, "Failed to add favorite", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "You need to be logged in to use favorites", Toast.LENGTH_SHORT).show()
        }
    }
}
