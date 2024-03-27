package com.gracedian.explorebojonegoro.ui.dashboard.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.item.User
import com.gracedian.explorebojonegoro.ui.dashboard.home.SearchActivity
import com.gracedian.explorebojonegoro.ui.welcome.WelcomeActivity
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var textViewProfile: TextView
    private lateinit var imgUserProfile: ImageView
    private lateinit var userNametxt: TextView
    private lateinit var emailUser: TextView
    private lateinit var detailProfile: LinearLayout
    private lateinit var updatePassword: LinearLayout
    private lateinit var historyPerjalanan: LinearLayout
    private lateinit var wishlist: LinearLayout
    private lateinit var logOut: LinearLayout
    private lateinit var loadingProgressBar: ProgressBar


    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userImageURL: String? = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        textViewProfile = view.findViewById(R.id.textViewProfile)
        imgUserProfile = view.findViewById(R.id.imgUserProfile)
        userNametxt = view.findViewById(R.id.userNametxt)
        emailUser = view.findViewById(R.id.emailUsertxt)
        detailProfile = view.findViewById(R.id.detailProfile)
        updatePassword = view.findViewById(R.id.updatePassword)
        historyPerjalanan = view.findViewById(R.id.historyPerjalanan)
        wishlist = view.findViewById(R.id.wishlist)
        logOut = view.findViewById(R.id.logOut)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)


        detailProfile.setOnClickListener {
            val intent = Intent(requireActivity(), MyProfileActivity::class.java)
            startActivity(intent)
        }

        updatePassword.setOnClickListener {
            val intent = Intent(requireActivity(), UpdatePasswordActivity::class.java)
            startActivity(intent)
        }

        historyPerjalanan.setOnClickListener {
            // Aksi saat historyPerjalanan diklik
        }

        wishlist.setOnClickListener {
            // Aksi saat wishlist diklik
        }

        logOut.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        imgUserProfile.setOnLongClickListener {
            imagePreview()
            true
        }
        getUsers()

        return view
    }
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Konfirmasi Logout")
        builder.setMessage("Anda yakin ingin logout?")
        builder.setPositiveButton("Ya") { dialog, which ->
            SharedPrefManager.logout(requireContext())
            SharedPrefManager.isFirstInstall(requireContext())
            auth = FirebaseAuth.getInstance()
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            auth.signOut()
            startActivity(intent)
            activity?.finishAffinity()
        }

        builder.setNegativeButton("Batal") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }
    private fun getUsers(){
        loadingProgressBar.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = database.reference.child("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.getValue(User::class.java)
                        val userName = userData?.name
                        userImageURL = userData?.profileImageUrl
                        val userEmail = userData?.email

                        if (userName != null) {
                            userNametxt.text = userName
                            emailUser.text = userEmail
                            userImageURL?.let { loadImageProfile(it) }

                        }
                    }
                    loadingProgressBar.visibility = View.GONE
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("error", error.message)
                    loadingProgressBar.visibility = View.GONE
                }
            })
        }
    }
    private fun loadImageProfile(imageURL: String) {
        Glide.with(requireContext())
            .load(imageURL)
            .placeholder(R.drawable.ic_user)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .into(imgUserProfile)
    }
    private fun imagePreview() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.image_preview_dialog)

        val previewImageView = dialog.findViewById<ImageView>(R.id.previewImageView)
        userImageURL?.let { imageURL ->
            Glide.with(requireContext())
                .load(imageURL)
                .placeholder(R.drawable.ic_user)
                .into(previewImageView)
        }

        dialog.show()
    }


}
