package com.gracedian.explorebojonegoro.ui.dashboard.profile

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.item.User
import java.io.ByteArrayOutputStream

class MyProfileActivity : AppCompatActivity() {

    private lateinit var inputName: TextInputEditText
    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputTlp: TextInputEditText
    private lateinit var imgProfile: ImageView
    private lateinit var btBack: ImageView
    private lateinit var btCancel: TextView
    private lateinit var autoCompleteGender: AutoCompleteTextView
    private lateinit var changeImgProfile: TextView
    private lateinit var editButton: AppCompatButton
    private lateinit var loadingBar: ProgressBar
    private var isEditing = false
    private var imageUri: Uri? = null

    private val genderOptions = arrayOf("Laki - Laki", "Perempuan")

    private lateinit var database: FirebaseDatabase
    private lateinit var storageReference: StorageReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        database = FirebaseDatabase.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        inputName = findViewById(R.id.inputNama)
        inputEmail = findViewById(R.id.inputEmail)
        inputTlp = findViewById(R.id.inputTlp)
        btBack = findViewById(R.id.btBack)
        btCancel = findViewById(R.id.btCancel)
        imgProfile = findViewById(R.id.imgUserProfile)
        changeImgProfile = findViewById(R.id.changeImg)
        autoCompleteGender = findViewById(R.id.autoCompleteGender)
        editButton = findViewById(R.id.buttonEditProfile)
        loadingBar = findViewById(R.id.loadingBar)

        inputEmail.isEnabled = false

        setFieldsEditable(false)
        btCancel.visibility = View.GONE
        changeImgProfile.visibility = View.GONE
        editButton.setOnClickListener {
            if (isEditing) {
                dialogFragment()
            } else {
                setFieldsEditable(true)
                changeImgProfile.visibility = View.VISIBLE
                btBack.visibility = View.VISIBLE
                btCancel.visibility = View.VISIBLE
                editButton.text = "Simpan Profile"
            }
            isEditing = !isEditing
        }

        btBack.setOnClickListener {
            if (editButton.text == "Simpan Profile") {
                setFieldsEditable(false)
                changeImgProfile.visibility = View.GONE
                btCancel.visibility = View.GONE
                editButton.text = "Edit Profile"
            } else if (editButton.text == "Edit Profile"){
                finish()
            }

        }
        btCancel.setOnClickListener {
            setFieldsEditable(false)
            changeImgProfile.visibility = View.GONE
            btCancel.visibility = View.GONE
            editButton.text = "Edit Profile"
            isEditing = !isEditing
        }

        changeImgProfile.setOnClickListener {
            selectImage()
        }


        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        autoCompleteGender.setAdapter(genderAdapter)

        autoCompleteGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = genderOptions[position]
                autoCompleteGender.setSelection(position)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where nothing is selected (optional)
            }
        }


        getUserData()
    }

    private fun setFieldsEditable(editable: Boolean) {
        inputName.isEnabled = editable
        inputTlp.isEnabled = editable
        autoCompleteGender.isEnabled = editable
    }

    private fun updateUserData() {
        val updatedName = inputName.text.toString()
        val updatedEmail = inputEmail.text.toString()
        val updatedTlp = inputTlp.text.toString()
        val updatedGender = autoCompleteGender.text.toString()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            loadingBar.visibility = View.VISIBLE
            val userId = user.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
            val updatedUser = User(updatedName, updatedEmail, updatedTlp, updatedGender)

            databaseReference.setValue(updatedUser).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (imageUri != null) {
                        saveImageToStorage(userId, imageUri!!)
                    } else {
                        Toast.makeText(this, "Data Profile berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        loadingBar.visibility = View.GONE
                        changeImgProfile.visibility = View.GONE
                        setFieldsEditable(false)
                        editButton.text = "Edit Profile"
                    }
                } else {
                    Toast.makeText(this, "Data Profile gagal diperbarui", Toast.LENGTH_SHORT).show()
                    loadingBar.visibility = View.GONE
                }
            }
        }
    }

    private fun getUserData() {
        loadingBar.visibility = View.VISIBLE
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            databaseReference.child(userId).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userData = dataSnapshot.getValue(User::class.java)

                        if (userData != null) {
                            inputName.setText(userData.name)
                            inputEmail.setText(userData.email)
                            inputTlp.setText(userData.tlp)
                            autoCompleteGender.setText(userData.gender)

                            val profileImageUrl = userData.profileImageUrl
                            Glide.with(this@MyProfileActivity)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_user)
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .into(imgProfile)
                        }
                        loadingBar.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    loadingBar.visibility = View.GONE
                }
            })
        }
    }

    private fun selectImage() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        try {
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Gallery is not available.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.data
            imageUri?.let { displaySelectedImage(it) }
        }
    }

    private fun displaySelectedImage(imageUri: Uri) {
        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .into(imgProfile)
    }

    private fun saveImageToStorage(userId: String, imageUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("profile_images/$userId.jpg")

        // Convert the selected image to a byte array
        val inputStream = contentResolver.openInputStream(imageUri)
        val buffer = ByteArray(4096)
        val baos = ByteArrayOutputStream()
        var bytesRead: Int
        if (inputStream != null) {
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                baos.write(buffer, 0, bytesRead)
            }
        }
        val data = baos.toByteArray()

        imageRef.putBytes(data)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    updateProfileWithImageDownloadURL(userId, downloadUrl)
                }
            }
            .addOnFailureListener {
                loadingBar.visibility = View.GONE
                Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfileWithImageDownloadURL(userId: String, downloadUrl: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userData = dataSnapshot.getValue(User::class.java)

                    if (userData != null) {
                        userData.profileImageUrl = downloadUrl
                        databaseReference.setValue(userData)
                            .addOnSuccessListener {
                                changeImgProfile.visibility = View.GONE
                                setFieldsEditable(false)
                                btCancel.visibility = View.GONE
                                editButton.text = "Edit Profile"
                                loadingBar.visibility = View.GONE
                                Toast.makeText(this@MyProfileActivity, "Data Profile dan gambar berhasil diperbarui", Toast.LENGTH_SHORT).show()

                            }
                            .addOnFailureListener { exception ->
                                loadingBar.visibility = View.GONE
                                Toast.makeText(this@MyProfileActivity, "Gagal mengupdate data profil", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                loadingBar.visibility = View.GONE
                Toast.makeText(this@MyProfileActivity, "Gagal mengupdate data profil", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun dialogFragment() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Simpan Perubahan")
        builder.setMessage("Apakah data yang Anda masukkan sudah benar?")
        builder.setPositiveButton("Ya") { dialog, which ->
            updateUserData()
        }

        builder.setNegativeButton("Batal") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    companion object {
        const val GALLERY_REQUEST_CODE = 123
    }
}
