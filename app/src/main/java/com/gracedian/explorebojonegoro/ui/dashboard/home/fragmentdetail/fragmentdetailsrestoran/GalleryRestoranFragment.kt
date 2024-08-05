package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter.GaleriAdapter
import java.io.ByteArrayOutputStream
import java.util.UUID

class GalleryRestoranFragment : Fragment() {
    private val PICK_IMAGE = 1
    private val galeriList = mutableListOf<String>()
    private lateinit var rcGallery: RecyclerView
    private lateinit var galeriAdapter: GaleriAdapter
    private lateinit var countTxt: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gallery_restoran, container, false)

        rcGallery = view.findViewById(R.id.rcgalery)
        countTxt = view.findViewById(R.id.countTxt)
        rcGallery.layoutManager = GridLayoutManager(requireContext(), 2)
        galeriAdapter = GaleriAdapter(galeriList)
        rcGallery.adapter = galeriAdapter

        view.findViewById<View>(R.id.btAddPhoto).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
        }
        getImages()

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                uploadImageToFirebaseStorage(imageUri)
            }
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Uploading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference
        val namaRestoran = arguments?.getString("namaRestoran")

        val fotoRef = storageRef.child("galeri/$namaRestoran/${UUID.randomUUID()}.jpg")

        val imageStream = requireActivity().contentResolver.openInputStream(imageUri)
        val selectedImage =
            MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
        val baos = ByteArrayOutputStream()
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        fotoRef.putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                Toast.makeText(requireContext(), "Berhasil mengunggah gambar", Toast.LENGTH_SHORT)
                    .show()
                galeriList.clear()
                getImages()
                progressDialog.dismiss()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Gagal mengunggah gambar: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog.dismiss()
            }
    }

    private fun getImages() {
        val namaRestoran = arguments?.getString("namaRestoran")
        val storage = FirebaseStorage.getInstance()
        val storageRef: StorageReference = storage.reference

        val galeriRef = storageRef.child("galeri/$namaRestoran")

        galeriRef.listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->
                    item.downloadUrl.addOnSuccessListener { uri ->
                        galeriList.add(uri.toString())
                        countTxt.text = "(${galeriList.size})"
                        galeriAdapter.notifyDataSetChanged()
                    }.addOnFailureListener { exception ->

                        Toast.makeText(
                            requireContext(),
                            "Gagal mendapatkan URL gambar: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Gagal mendapatkan daftar gambar: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }
}