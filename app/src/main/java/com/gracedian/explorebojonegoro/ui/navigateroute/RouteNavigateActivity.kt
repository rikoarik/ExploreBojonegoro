package com.gracedian.explorebojonegoro.ui.navigateroute

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.gracedian.explorebojonegoro.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RouteNavigateActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val apiKeyMapQuest = "FW0RdJUe26F0Hos31UdL00zgWcEoZDGU"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_navigate)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        val latFrom = intent.getDoubleExtra("latOrigin", 0.0)
        val longFrom = intent.getDoubleExtra("longOrigin", 0.0)
        val latTo = intent.getDoubleExtra("latDestination", 0.0)
        val longTo = intent.getDoubleExtra("longDestination", 0.0)

        // Convert latFrom and longFrom to a string
        val origin = "$latFrom,$longFrom"
        val destination = "$latTo,$longTo"

        // Make the API request
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.mapquestapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val mapQuestService = retrofit.create(MapQuestService::class.java)
        val call = mapQuestService.getRoute(apiKeyMapQuest, origin, destination)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    val route = response.body()?.route
                    val shapePoints = route?.shape?.shapePoints

                    // Draw the polyline on the map
                    shapePoints?.let {
                        withContext(Dispatchers.Main) {
                            val polylineOptions = PolylineOptions()
                                .width(8f)
                                .color(Color.BLUE)

                            // Convert shapePoints into LatLng objects
                            for (i in shapePoints.indices step 2) {
                                val lat = shapePoints[i]
                                val lng = shapePoints[i + 1]
                                polylineOptions.add(LatLng(lat, lng))
                            }

                            map.addPolyline(polylineOptions)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latFrom, longFrom), 12f))
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RouteNavigateActivity, "Failed to retrieve route", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("RouteNavigateActivity", "Error fetching route", e)
            }
        }
    }

}
