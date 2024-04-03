package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R

class TentangRestoranFragment : Fragment() {

    private lateinit var descTxt: TextView
    private lateinit var listLainLain: TextView
    private lateinit var namaRestoran: String


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tentang_restoan, container, false)
        namaRestoran = arguments?.getString("namaRestoran").toString()

        descTxt = view.findViewById(R.id.descTxt)
        listLainLain = view.findViewById(R.id.listLainLain)
        getData()
        return view
    }

    private fun getData() {
        val db = FirebaseDatabase.getInstance().getReference("Restoran")
        val query = db.orderByChild("Restoran").equalTo(namaRestoran)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        val deskripsi = childSnapshot.child("deskripsi").getValue(String::class.java)
                        val lainLain = childSnapshot.child("lainLain").getValue(String::class.java)

                        descTxt.text = deskripsi
                        listLainLain.text = lainLain
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("GetData", "Error: ${databaseError.message}")
            }
        })
    }
}