package com.gracedian.explorebojonegoro.ui.dashboard.maps

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.DetailsWisataActivity
import com.gracedian.explorebojonegoro.ui.dashboard.home.adapter.WisataTerdekatAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.PopularItem
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class MapsFragment : Fragment(), OnMapReadyCallback, WisataTerdekatAdapter.OnItemClickListener {

    private lateinit var mapView: MapView
    private lateinit var mMap: GoogleMap
    private lateinit var rcWisataMaps: RecyclerView
    private lateinit var wisataTerdekatAdapter: WisataTerdekatAdapter
    private val wisataTerdekatList = mutableListOf<WisataTerdekatItem>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        rcWisataMaps = view.findViewById(R.id.rcWisataMaps)

        rcWisataMaps.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
        wisataTerdekatAdapter = WisataTerdekatAdapter(wisataTerdekatList, this)
        rcWisataMaps.adapter = wisataTerdekatAdapter


        return view
    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.isMyLocationEnabled = true

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        currentLocation = it
                        getDataWisataTerdekat()
                    }
                }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
    private fun getDataWisataTerdekat() {
        val db = FirebaseDatabase.getInstance().getReference("objekwisata")
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMap.clear() // Clear existing markers
                    wisataTerdekatList.clear()
                    for (childSnapshot in dataSnapshot.children) {
                        val wisata = childSnapshot.child("wisata").getValue(String::class.java)
                        val alamat = childSnapshot.child("alamat").getValue(String::class.java)
                        val latString = childSnapshot.child("latitude").getValue(String::class.java)
                        val longString = childSnapshot.child("longitude").getValue(String::class.java)
                        val imageUrl = childSnapshot.child("imageUrl").getValue(String::class.java)

                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0

                        // Create LatLng object for the attraction
                        val latLng = LatLng(lat, long)

                        // Create marker options
                        val markerOptions = MarkerOptions().position(latLng).title(wisata)

                        // Add marker to the map
                        mMap.addMarker(markerOptions)

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))

                        // Hitung jarak antara lokasi saat ini dan lokasi wisata
                        val jarak = calculateVincentyDistance(currentLocation?.latitude ?: 0.0, currentLocation?.longitude ?: 0.0, lat, long) / 1000

                        val intValue = jarak.toInt()

                        val wisataTerdekatItem = WisataTerdekatItem(
                            imageUrl = imageUrl,
                            wisata = wisata,
                            rating = 0.toFloat(),
                            alamat = alamat,
                            jarak = intValue,
                            lat = lat,
                            long = long,
                        )
                        wisataTerdekatList.add(wisataTerdekatItem)

                    }
                    wisataTerdekatList.sortBy { it.jarak }
                    rcWisataMaps.adapter = wisataTerdekatAdapter
                    getFavoriteItems()
                    wisataTerdekatAdapter.notifyDataSetChanged()
                }
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
        val searchItem = wisataTerdekatList[position]
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val favoritesRef = FirebaseDatabase.getInstance().reference.child("favorites").child(userId)

            val newFavoriteStatus = !searchItem.isFavorite

            if (newFavoriteStatus) {
                searchItem.wisata?.let {
                    favoritesRef.child(it).setValue(true)
                        .addOnSuccessListener {
                            val imageView = rcWisataMaps.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
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
                            val imageView = rcWisataMaps.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)
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
            val favoritesRef = FirebaseDatabase.getInstance().getReference("favorites").child(uid)
            favoritesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val itemId = childSnapshot.key
                        val isFavorite = childSnapshot.getValue(Boolean::class.java)

                        for (item in wisataTerdekatList) {
                            if (item.wisata == itemId) {
                                item.isFavorite = isFavorite ?: false
                                val position = wisataTerdekatList.indexOf(item)
                                val imageView = rcWisataMaps.layoutManager?.findViewByPosition(position)?.findViewById<ImageView>(R.id.btFavorite)

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


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
