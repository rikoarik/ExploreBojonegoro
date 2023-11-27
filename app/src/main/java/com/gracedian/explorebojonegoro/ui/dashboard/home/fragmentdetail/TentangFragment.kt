package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R

class TentangFragment : Fragment() {
    private lateinit var descTxt: TextView
    private lateinit var rcFasilitas: RecyclerView
    private lateinit var listLainLain: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tentang, container, false)

        descTxt = view.findViewById(R.id.descTxt)
        rcFasilitas = view.findViewById(R.id.rcFasilitas)
        listLainLain = view.findViewById(R.id.listLainLain)




//        // Inisialisasi RecyclerView dan tampilkan data fasilitas jika ada
//        val fasilitasList: List<String> = /* ambil data fasilitas dari argument atau sumber data lainnya */
//            rcFasilitas.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
//        val fasilitasAdapter = FasilitasAdapter(fasilitasList) // Sesuaikan dengan adapter yang Anda miliki
//        rcFasilitas.adapter = fasilitasAdapter
//
//        // Inisialisasi ListView dan tampilkan data "Lain-Lain" jika ada
//        val lainLainList: List<String> = /* ambil data "Lain-Lain" dari argument atau sumber data lainnya */
//        val lainLainAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, lainLainList)
//        listLainLain.adapter = lainLainAdapter
        getData()
        return view
    }
    private fun getData() {
        val namawisata = arguments?.getString("namaWisata")
        val db = FirebaseDatabase.getInstance().getReference("datawisata")
        val query =  db.orderByChild("wisata").equalTo(namawisata)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val latString = childSnapshot.child("lat").getValue(String::class.java)
                        val longString = childSnapshot.child("long").getValue(String::class.java)


                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }

}
