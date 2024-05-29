package com.gracedian.explorebojonegoro.ui.dashboard.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.*
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.item.User
import com.gracedian.explorebojonegoro.ui.dashboard.home.activity.DetailsWisataActivity
import com.gracedian.explorebojonegoro.ui.dashboard.home.activity.SearchActivity
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.PopularAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.PopularAdapter.OnItemClickListener
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.WisataTerdekatAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.apiservice.WeatherRetrofit
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.PopularItem
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import com.gracedian.explorebojonegoro.utils.distancecalculate.calculateVincentyDistance
import kotlinx.coroutines.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment(), WisataTerdekatAdapter.OnItemClickListener, OnItemClickListener{

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private lateinit var tgltxt: TextView
    private lateinit var jamtxt: TextView
    private lateinit var suhutxt: TextView
    private lateinit var ketCuaca: TextView
    private lateinit var icCuaca: ImageView
    private lateinit var btSearch: ImageView
    private lateinit var locSaatIni: TextView
    private lateinit var userNametxt: TextView
    private lateinit var imgUserProfile: ImageView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout


    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private lateinit var rcWisataTerdekat: RecyclerView
    private lateinit var rcWisataPopular: RecyclerView
    private lateinit var wisataTerdekatAdapter: WisataTerdekatAdapter
    private lateinit var popularAdapter: PopularAdapter
    private val wisataPopularList = mutableListOf<PopularItem>()
    private val wisataTerdekatList = mutableListOf<WisataTerdekatItem>()
    private var currentLocation: Location? = null
    private var averageRating = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        tgltxt = view.findViewById(R.id.tgltxt)
        jamtxt = view.findViewById(R.id.jamtxt)
        suhutxt = view.findViewById(R.id.suhutxt)
        ketCuaca = view.findViewById(R.id.ketCuaca)
        icCuaca = view.findViewById(R.id.icCuaca)
        locSaatIni = view.findViewById(R.id.locSaatIni)
        userNametxt = view.findViewById(R.id.userNametxt)
        imgUserProfile = view.findViewById(R.id.imgUserProfile)
        btSearch = view.findViewById(R.id.btSearch)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)

        rcWisataTerdekat = view.findViewById(R.id.rcWisataTerdekat)
        rcWisataPopular = view.findViewById(R.id.rcPopuler)
        
        rcWisataTerdekat.layoutManager = LinearLayoutManager(requireContext())
        wisataTerdekatAdapter = WisataTerdekatAdapter(wisataTerdekatList, this)
        rcWisataTerdekat.adapter = wisataTerdekatAdapter

        rcWisataPopular.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
        popularAdapter = PopularAdapter(wisataPopularList, this)
        rcWisataPopular.adapter = popularAdapter

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        handler = Handler()
        runnable = Runnable { updateDateTime() }
        handler.post(runnable)

        btSearch.setOnClickListener {
            val lat = currentLocation?.latitude
            val long = currentLocation?.longitude
            val intent = Intent(requireActivity(), SearchActivity::class.java)
            intent.putExtra("lat", lat)
            intent.putExtra("long", long)
            startActivity(intent)
        }

        loadingProgressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            getUser()
            checkPermission()
            getDataWisataTerdekat()
            getFavoriteItems()
        }
    }

    private fun refreshData() {
        loadingProgressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            checkPermission()
            getUser()
            getDataWisataTerdekat()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun updateDateTime() {
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
        val timeFormat = SimpleDateFormat("HH.mm", Locale("id", "ID"))

        val dateText = dateFormat.format(currentTime)
        val timeText = timeFormat.format(currentTime)
        tgltxt.text = dateText
        jamtxt.text = timeText
        handler.postDelayed(runnable, 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }

    private fun checkPermission(){
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, get the location
            getLocation()
        }
    }
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let { loc ->
                    currentLocation = loc
                    val latitude = loc.latitude
                    val longitude = loc.longitude
                    context?.let { ctx ->
                        val geocoder = Geocoder(ctx, Locale.getDefault())
                        try {
                            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                            addresses?.firstOrNull()?.locality?.let { cityName ->
                                locSaatIni.text = cityName
                                getWeatherData(latitude, longitude)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
    }

    private fun getUser() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = database.reference.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.getValue(User::class.java)
                        val userName = userData?.name
                        val userImageURL = userData?.profileImageUrl

                        if (userName != null) {
                            userNametxt.text = userName
                            userImageURL?.let { loadImageProfile(it) }
                            loadingProgressBar.visibility = View.GONE
                        }
                    }
                    loadingProgressBar.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", error.message)
                    loadingProgressBar.visibility = View.GONE
                }
            })
        }
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        val weatherRetrofit = WeatherRetrofit()
        weatherRetrofit.getWeatherData(latitude, longitude) { weatherResponse ->
            if (weatherResponse != null) {
                val temperature = weatherResponse.main.temp
                val weatherDescription = weatherResponse.weather[0].description
                val iconId = weatherResponse.weather[0].icon


                suhutxt.text = "${temperature.toInt()} °C"
                ketCuaca.text = weatherDescription

                val iconUrl = "https://openweathermap.org/img/wn/$iconId@4x.png"
                Log.d("icon", iconId.toString())
                Glide.with(this)
                    .load(iconUrl)
                    .into(icCuaca)
                val suitableForVacation = weatherDescription?.let { isWeatherSuitableForVacation(it) }
                if (suitableForVacation == true) {
                    showToast("Cuaca sangat bagus untuk liburan!")
                } else {
                    showToast("Cuaca mungkin tidak ideal untuk liburan.")
                }
            } else {
                Log.e("Error", "Gagal mendapatkan data cuaca")
            }
        }
    }
    private fun isWeatherSuitableForVacation(description: String): Boolean {
        val suitableWeatherConditions = listOf(
            "clear sky",
            "few clouds",
            "scattered clouds",
            "broken clouds"
        )

        return suitableWeatherConditions.any { condition ->
            description.contains(condition, ignoreCase = true)
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }


    private fun loadImageProfile(imageURL: String) {
        Glide.with(requireContext())
            .load(imageURL)
            .placeholder(R.drawable.ic_user)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .into(imgUserProfile)
    }
    private fun getDataWisataTerdekat() {
        val db = FirebaseDatabase.getInstance().getReference("objekwisata")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val previousFavorites = mutableMapOf<String, Boolean>() // Store previous favorite status
                for (item in wisataTerdekatList) {
                    previousFavorites[item.wisata ?: ""] = item.isFavorite
                }
                if (dataSnapshot.exists()) {
                    wisataTerdekatList.clear()
                    wisataPopularList.clear()
                    for (childSnapshot in dataSnapshot.children) {
                        val wisata = childSnapshot.child("wisata").getValue(String::class.java)
                        val alamat = childSnapshot.child("alamat").getValue(String::class.java)
                        val latString = childSnapshot.child("latitude").getValue(String::class.java)
                        val longString = childSnapshot.child("longitude").getValue(String::class.java)
                        val imageUrl = childSnapshot.child("imageUrl").getValue(String::class.java)

                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0

                        // Hitung jarak antara lokasi saat ini dan lokasi wisata
                        val jarak = calculateVincentyDistance(currentLocation?.latitude ?: 0.0, currentLocation?.longitude ?: 0.0, lat, long) / 1000

                        val intValue = jarak.toInt()

                        val wisataTerdekatItem = WisataTerdekatItem(
                            imageUrl = imageUrl,
                            wisata = wisata,
                            rating = averageRating,
                            alamat = alamat,
                            jarak = intValue,
                            lat = lat,
                            long = long,
                        )
                        wisataTerdekatList.add(wisataTerdekatItem)
                        val wisataPopularItem = PopularItem(
                            imageUrl = imageUrl,
                            namaWisata = wisata,
                            lokasiWisata = alamat,
                            rating = averageRating
                        )
                        wisataPopularList.add(wisataPopularItem)
                        if (wisata != null) {
                            setRatingTextByWisataName(wisata)
                        }
                    }

                    wisataTerdekatList.sortBy { it.jarak }
                    wisataPopularList.sortByDescending { it.rating }
                    rcWisataPopular.adapter = popularAdapter
                    popularAdapter.notifyDataSetChanged()
                    rcWisataTerdekat.adapter = wisataTerdekatAdapter
                    wisataTerdekatAdapter.notifyDataSetChanged()
                }

                loadingProgressBar.visibility = View.GONE
                getFavoriteItems()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }




    override fun onItemTerdekatClick(position: Int) {
        val terdekatItem = wisataTerdekatList[position]
        val intent = Intent(requireActivity(), DetailsWisataActivity::class.java)
        intent.putExtra("wisata", terdekatItem.wisata)
        startActivity(intent)
    }


    override fun onFavoriteClick(position: Int) {
        val favoriteItem = wisataTerdekatList[position]
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val favoritesRef = database.reference.child("favorites").child(userId)

            val newFavoriteStatus = !favoriteItem.isFavorite

            if (newFavoriteStatus) {
                favoriteItem.wisata?.let {
                    favoritesRef.child(it).setValue(true)
                        .addOnSuccessListener {
                            val imageView = rcWisataTerdekat.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
                            imageView?.setImageResource(R.drawable.ic_favorite_true)
                            showToast("Wisata ditambahkan ke wishlist")
                        }
                        .addOnFailureListener { e ->
                            showToast("Gagal untuk menambahkan wisata ke wihslist: ${e.message}")
                        }
                }
            } else {
                favoriteItem.wisata?.let {
                    favoritesRef.child(it).setValue(false)
                        .addOnSuccessListener {
                            val imageView = rcWisataTerdekat.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
                            imageView?.setImageResource(R.drawable.ic_favorite_false)
                            showToast("Wisata dihapus dari wishlist")
                        }
                        .addOnFailureListener { e ->
                            showToast("Gagal untuk menghapus wisata dari wihslist: ${e.message}")
                        }
                }
            }
        }
    }



    override fun onItemPopularClick(position: Int) {
        val popularItem = wisataPopularList[position]
        val intent = Intent(requireActivity(), DetailsWisataActivity::class.java)
        intent.putExtra("wisata", popularItem.namaWisata)
        startActivity(intent)
    }

    private fun getFavoriteItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val favoritesRef = database.reference.child("favorites").child(uid)
            favoritesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (childSnapshot in snapshot.children) {
                        val itemId = childSnapshot.key
                        val isFavorite = childSnapshot.getValue(Boolean::class.java)

                        for (item in wisataTerdekatList) {
                            if (item.wisata == itemId) {
                                item.isFavorite = isFavorite ?: false
                                val position = wisataTerdekatList.indexOf(item)
                                val imageView = rcWisataTerdekat.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)

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
                    for (item in wisataTerdekatList) {
                        if (item.wisata == wisata) {
                            item.rating = averageRating
                        }
                    }

                    wisataTerdekatAdapter.notifyDataSetChanged()
                    for (item in wisataPopularList) {
                        if (item.namaWisata == wisata) {
                            item.rating = averageRating
                            Log.e("ratingg", item.rating.toString())
                        }
                    }

                    popularAdapter.notifyDataSetChanged()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
                // Handle error
            }
        })
    }



}
