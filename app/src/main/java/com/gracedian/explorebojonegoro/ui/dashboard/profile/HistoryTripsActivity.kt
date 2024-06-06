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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.activity.DetailsWisataActivity
import com.gracedian.explorebojonegoro.ui.dashboard.home.items.WisataTerdekatItem
import com.gracedian.explorebojonegoro.ui.dashboard.profile.adapter.HistoryTripsAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.profile.item.HistoryTrips
import com.gracedian.explorebojonegoro.utils.distancecalculate.calculateVincentyDistance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryTripsActivity : AppCompatActivity(), HistoryTripsAdapter.OnItemClickListener {

    private lateinit var btBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryTripsAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val historyItems = mutableListOf<HistoryTrips>()
    private var currentLocation: Location? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var editTextSearch: EditText
    private val filteredHistoryTripsItems = mutableListOf<HistoryTrips>()

    private var date: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_trips)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        databaseReference = FirebaseDatabase.getInstance().reference

        editTextSearch = findViewById(R.id.editTextSearch)
        recyclerView = findViewById(R.id.rcMyTrips)
        btBack = findViewById(R.id.btBack)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryTripsAdapter(filteredHistoryTripsItems, this)
        recyclerView.adapter = adapter

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
        CoroutineScope(Dispatchers.Main).launch {
            getLocation()
            getDestinationsFromFirebase()
        }

    }
    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
                }
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun filterWishlistItems(query: String) {
        filteredHistoryTripsItems.clear()
        if (query.isEmpty()) {
            filteredHistoryTripsItems.addAll(historyItems)
        } else {
            filteredHistoryTripsItems.addAll(historyItems.filter { item ->
                item.wisata?.contains(query, ignoreCase = true) == true || item.alamat?.contains(query, ignoreCase = true) == true
            })
        }
        adapter.notifyDataSetChanged()
    }



    private fun getDestinationsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let {
            val destinationsRef = databaseReference.child("users").child(userId).child("destinations")
            destinationsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (destinationSnapshot in dataSnapshot.children) {
                        val name = destinationSnapshot.child("name").getValue(String::class.java)
                        date = destinationSnapshot.child("date").getValue(String::class.java)!!
                        if(name != null){
                            getDataWisataTerdekat(name)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("Failed to read value:", databaseError.toException().toString())
                }
            })
        }
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
                        val longString =
                            childSnapshot.child("longitude").getValue(String::class.java)
                        val imageUrl = childSnapshot.child("imageUrl").getValue(String::class.java)

                        val lat = latString?.toDoubleOrNull() ?: 0.0
                        val long = longString?.toDoubleOrNull() ?: 0.0

                        val jarak = calculateVincentyDistance(
                            currentLocation?.latitude ?: 0.0,
                            currentLocation?.longitude ?: 0.0,
                            lat,
                            long
                        ) / 1000

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
                        val historyTrip = HistoryTrips(
                            imageUrl = wisataTerdekatItem.imageUrl,
                            wisata = wisataTerdekatItem.wisata,
                            alamat = wisataTerdekatItem.alamat,
                            date = date
                        )
                        historyItems.add(historyTrip)

                    }
                    recyclerView.adapter = adapter
                    filterWishlistItems(editTextSearch.text.toString().trim())
                    adapter.notifyDataSetChanged()

                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }

    override fun onItemTerdekatClick(position: Int) {
        val terdekatItem = historyItems[position]
        val intent = Intent(this, DetailsWisataActivity::class.java)
        intent.putExtra("wisata", terdekatItem.wisata)
        startActivity(intent)
    }
}