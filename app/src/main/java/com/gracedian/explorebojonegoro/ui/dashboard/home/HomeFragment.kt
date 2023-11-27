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
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.PopularAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.PopularAdapter.OnItemClickListener
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.WisataTerdekatAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.apiservice.WeatherRetrofit
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.PopularItem
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import kotlinx.coroutines.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class HomeFragment : Fragment(), WisataTerdekatAdapter.OnItemClickListener, PopularAdapter.OnItemClickListener{

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
        getUser()
        checkPermission()
        getDataWisataTerdekat()
    }

    private fun refreshData() {
        loadingProgressBar.visibility = View.VISIBLE
        checkPermission()
        getUser()
        getDataWisataTerdekat()
        swipeRefreshLayout.isRefreshing = false
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
    private fun getLocation(){
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = it
                    val latitude = it.latitude
                    val longitude = it.longitude
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        if (addresses!!.isNotEmpty()) {
                            val cityName = addresses[0].locality
                            locSaatIni.text = cityName
                            getWeatherData(latitude, longitude)

                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
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
        val db = FirebaseDatabase.getInstance().getReference("datawisata")
        db.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    wisataTerdekatList.clear()
                    wisataPopularList.clear()
                    for (childSnapshot in dataSnapshot.children) {
                        val wisata = childSnapshot.child("wisata").getValue(String::class.java)
                        val alamat = childSnapshot.child("alamat").getValue(String::class.java)
                        val latString = childSnapshot.child("lat").getValue(String::class.java)
                        val longString = childSnapshot.child("long").getValue(String::class.java)

                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0


                        // Hitung jarak antara lokasi saat ini dan lokasi wisata
                        val jarak = calculateVincentyDistance(currentLocation?.latitude ?: 0.0, currentLocation?.longitude ?: 0.0, lat, long) / 1000

                        val intValue = jarak.toInt()

                        val wisataTerdekatItem = WisataTerdekatItem(
                            imageUrl = "https://mmc.tirto.id/image/otf/880x495/2019/10/29/objek-wisata-kayangan-api-dinbudpar.bojonegorokab_1_ratio-16x9.jpg",
                            wisata = wisata,
                            rating = 0.toFloat(),
                            alamat = alamat,
                            jarak = intValue,
                            lat = lat,
                            long = long,
                        )
                        wisataTerdekatList.add(wisataTerdekatItem)
                        val wisataPopularItem = PopularItem(
                            imageUrl = "https://mmc.tirto.id/image/otf/880x495/2019/10/29/objek-wisata-kayangan-api-dinbudpar.bojonegorokab_1_ratio-16x9.jpg",
                            namaWisata = wisata,
                            lokasiWisata = alamat,
                            rating = 3.4
                        )
                        wisataPopularList.add(wisataPopularItem)

                    }
                    wisataTerdekatList.sortBy { it.jarak }
                    wisataPopularList.sortByDescending { it.rating }
                    rcWisataPopular.adapter = popularAdapter
                    popularAdapter.notifyDataSetChanged()
                    rcWisataTerdekat.adapter = wisataTerdekatAdapter
                    wisataTerdekatAdapter.notifyDataSetChanged()
                }
                loadingProgressBar.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
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

    override fun onItemTerdekatClick(position: Int) {
        val terdekatItem = wisataTerdekatList[position]
        val intent = Intent(requireActivity(), DetailsWisataActivity::class.java)
        intent.putExtra("wisata", terdekatItem.wisata)
        startActivity(intent)
    }


    override fun onFavoriteClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onItemPopularClick(position: Int) {
        val popularItem = wisataPopularList[position]
        val intent = Intent(requireActivity(), DetailsWisataActivity::class.java)
        intent.putExtra("wisata", popularItem.namaWisata)
        startActivity(intent)
    }


}
