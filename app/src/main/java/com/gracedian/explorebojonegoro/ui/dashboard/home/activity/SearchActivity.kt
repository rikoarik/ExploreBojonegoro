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

class SearchActivity : AppCompatActivity(), SearchAdapter.OnItemClickListener{

    private lateinit var backButton: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var searchLayout: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var filterButton: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var searchRecyclerView: RecyclerView
    private lateinit var notFoundImageView: ImageView

    val searchItemsList =  mutableListOf<SearchItem>()
    var searchAdapter = SearchAdapter(searchItemsList, this)

    var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("objekwisata")

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
            @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
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
                        if (isCategoryMatch(searchItem) && isRatingMatch(searchItem) && isJarakMaxMatch(searchItem)) {
                            searchItemsList.add(searchItem)
                        }
                        if (title != null) {
                            setRatingTextByWisataName(title)
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
    fun filterSearchResults(query: String) {
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
            resultTextView.text = "0 hasil ditemukan"
        }
    }

    @Deprecated("Deprecated in Java")
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
                            showToast("Wisata ditambahkan ke wishlist")
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

                    totalReviews = max(totalReviews, 1)

                    val averageRating = totalRating / totalReviews
                    for (item in searchItemsList) {
                        if (item.wisata == wisata) {
                            item.rating = averageRating
                            // Memberi tahu adapter bahwa data telah berubah
                            searchAdapter.notifyDataSetChanged()
                            break
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
    fun Context.showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}