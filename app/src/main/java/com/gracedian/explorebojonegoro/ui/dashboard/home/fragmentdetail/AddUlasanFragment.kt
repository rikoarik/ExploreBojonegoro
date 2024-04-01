package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail

import android.os.Bundle
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.items.Ulasan

class AddUlasanFragment : SuperBottomSheetFragment() {

    private lateinit var namaWisata: TextView
    private lateinit var locWisata: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var editTextTextMultiLine: EditText
    private lateinit var btnApply: AppCompatButton
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_add_ulasan, container, false)

        databaseReference = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        namaWisata = view.findViewById(R.id.namaWisata)
        locWisata = view.findViewById(R.id.locWisata)
        ratingBar = view.findViewById(R.id.ratingBar)
        editTextTextMultiLine = view.findViewById(R.id.editTextTextMultiLine)
        btnApply = view.findViewById(R.id.btnApply)

        val namaWisataText = arguments?.getString("namaWisata")
        val alamatWisata = arguments?.getString("alamat")
        namaWisata.text = namaWisataText
        locWisata.text = alamatWisata
        btnApply.setOnClickListener {
            val ulasan = editTextTextMultiLine.text.toString()
            val rating = ratingBar.rating.toDouble()

            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
            val namaObjekWisata = namaWisata.text.toString()

            val databaseReference = FirebaseDatabase.getInstance().reference

            currentUserUid?.let { uid ->
                databaseReference.child("ulasan")
                    .child(namaObjekWisata)
                    .child(uid)
                    .setValue(Ulasan(rating, ulasan))
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Ulasan Berhasil disimpan", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        // Gagal menyimpan ulasan ke Firebase Database
                    }
            }
        }
        return view
    }
}