package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter.UlasanAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.UlasanItems

class UlasanFragment : Fragment() {

    private lateinit var rcUlasan: RecyclerView
    private lateinit var btAddUlasan: LinearLayout
    private lateinit var ulasanList: MutableList<UlasanItems>
    private lateinit var ulasanAdapter: UlasanAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ulasan, container, false)
        databaseReference = FirebaseDatabase.getInstance().reference
        btAddUlasan = view.findViewById(R.id.btAddUlasan)
        rcUlasan = view.findViewById(R.id.rcUlasan)
        rcUlasan.layoutManager = LinearLayoutManager(requireContext())
        ulasanList = mutableListOf()
        ulasanAdapter = UlasanAdapter(ulasanList)
        rcUlasan.adapter = ulasanAdapter

        val namaWisataText = arguments?.getString("namaWisata")
        val alamatWisata = arguments?.getString("alamat")

        btAddUlasan.setOnClickListener {
            val ulasanDialog = AddUlasanFragment()
            val bundle = Bundle().apply {
                putString("namaWisata", namaWisataText)
                putString("alamat", alamatWisata)
            }
            ulasanDialog.arguments = bundle
            ulasanDialog.show(parentFragmentManager, "AddUlasanFragment")
        }
        if (namaWisataText != null) {
            fetchUlasanData(namaWisataText)
        }
        return view
    }
    private fun fetchUlasanData(wisata: String) {
        databaseReference.child("Ulasan").child(wisata).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val reversedUlasanList = mutableListOf<UlasanItems>()
                    for (ulasanSnapshot in snapshot.children) {
                        val dateAdded = ulasanSnapshot.child("dateAdded").getValue(String::class.java)
                        val ulasan = ulasanSnapshot.child("ulasan").getValue(String::class.java)
                        val rating = ulasanSnapshot.child("rating").getValue(Double::class.java)
                        val imageUser = ulasanSnapshot.child("imageUser").getValue(String::class.java)
                        val userName = ulasanSnapshot.child("userName").getValue(String::class.java)
                        val timeAgo = getTimeAgo(dateAdded?.toLong() ?: 0)


                        val ulasanItem = UlasanItems(imageUser, userName, timeAgo, ulasan, rating)

                        reversedUlasanList.add(ulasanItem)
                    }
                    ulasanList.clear()
                    ulasanList.addAll(reversedUlasanList.reversed())
                    ulasanAdapter.notifyDataSetChanged()
                }

                ulasanAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })


    }
    fun getTimeAgo(time: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - time

        return when {
            timeDifference < 1000 -> "Baru saja" // Jika perbedaan waktu kurang dari 1 detik
            timeDifference < 60000 -> "${timeDifference / 1000} detik yang lalu" // Jika perbedaan waktu kurang dari 1 menit
            timeDifference < 3600000 -> "${timeDifference / 60000} menit yang lalu" // Jika perbedaan waktu kurang dari 1 jam
            timeDifference < 86400000 -> "${timeDifference / 3600000} jam yang lalu" // Jika perbedaan waktu kurang dari 1 hari
            timeDifference < 2592000000 -> "${timeDifference / 86400000} hari yang lalu" // Jika perbedaan waktu kurang dari 30 hari (sekitar 1 bulan)
            timeDifference < 31536000000 -> "${timeDifference / 2592000000} bulan yang lalu" // Jika perbedaan waktu kurang dari 1 tahun
            else -> "${timeDifference / 31536000000} tahun yang lalu" // Jika perbedaan waktu lebih dari 1 tahun
        }
    }


}
