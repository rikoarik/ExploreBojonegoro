package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.item.User
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.UlasanItems


class AddUlasanRestoranFragment : SuperBottomSheetFragment() {
    private lateinit var namaRestoran: TextView
    private lateinit var locRestoran: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var editTextTextMultiLine: EditText
    private lateinit var btnApply: AppCompatButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_add_ulasan_restoran, container, false)

        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        namaRestoran = view.findViewById(R.id.namaRestoran)
        locRestoran = view.findViewById(R.id.locRestoran)
        ratingBar = view.findViewById(R.id.ratingBar)
        editTextTextMultiLine = view.findViewById(R.id.editTextTextMultiLine)
        btnApply = view.findViewById(R.id.btnApply)

        val namaRestoranText = arguments?.getString("namaRestoran")
        val alamatRestoran = arguments?.getString("alamat")
        namaRestoran.text = namaRestoranText
        locRestoran.text = alamatRestoran

        btnApply.setOnClickListener {
            val ulasan = editTextTextMultiLine.text.toString()
            val rating = ratingBar.rating.toDouble()

            val databaseReference = FirebaseDatabase.getInstance().reference
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                val namaObjekRestoran = namaRestoran.text.toString()

                val ulasanId = databaseReference.child("UlasanRestoran").push().key ?: ""
                val currentUserRef = databaseReference.child("UlasanRestoran").child(namaObjekRestoran).child(ulasanId)
                val dateAdded = System.currentTimeMillis().toString()

                userId.let { uid ->
                    databaseReference
                        .child("users")
                        .child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val userData = snapshot.getValue(User::class.java)
                                    val userName = userData?.name
                                    val userImageURL = userData?.profileImageUrl

                                    currentUserRef.setValue(
                                        UlasanItems(
                                            userImageURL,
                                            userName,
                                            dateAdded,
                                            ulasan,
                                            rating
                                        )
                                    )
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                requireContext(),
                                                "Ulasan Berhasil disimpan",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            dismiss()
                                        }
                                        .addOnFailureListener {
                                            Log.e(
                                                "AddUlasanFragment",
                                                "Gagal menyimpan ulasan ke Firebase Database",
                                                it
                                            )
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("error", error.message)
                            }
                        })
                }
            }
        }


        return view
    }
}