package com.gracedian.explorebojonegoro.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.DashboardActivity

class SignInActivity : AppCompatActivity() {

    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputPassword: TextInputEditText
    private lateinit var inputEmailLayout: TextInputLayout
    private lateinit var inputPasswordLayout: TextInputLayout
    private lateinit var btBack: ImageView
    private lateinit var loading: ProgressBar
    private lateinit var buttonLogin: AppCompatButton
    private lateinit var btRegister: TextView
    private lateinit var btLupaSandi: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        inputEmailLayout = findViewById(R.id.inputLayoutEmail)
        inputPasswordLayout = findViewById(R.id.inputLayoutPassword)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        btBack = findViewById(R.id.btBack)
        btLupaSandi = findViewById(R.id.lupaPassword)

        buttonLogin = findViewById(R.id.buttonLogin)
        btRegister = findViewById(R.id.btRegister)
        loading = findViewById(R.id.loadingBar)

        btBack.setOnClickListener {
            finish()
        }
        btLupaSandi.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }

        inputEmail.addTextChangedListener(createTextWatcher(inputEmailLayout) { text ->
            showErrorEmail(inputEmailLayout, text)
        })
        inputPassword.addTextChangedListener(createTextWatcher(inputPasswordLayout) { text ->
            showErrorPassword(inputPasswordLayout, text)
        })
        buttonLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            loading.visibility = View.VISIBLE
            buttonLogin.isClickable = false
            if (isInputValid(email, password)) {
                login(email, password)
                buttonLogin.isClickable = true

            }
        }

        btRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userId = user.uid
                        val userRef = database.reference.child("users").child(userId)

                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val userData = snapshot.value as? Map<*, *>

                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                        SharedPrefManager.setLoggedIn(this, true)
                        val intent = Intent(this, DashboardActivity::class.java)

                        startActivity(intent)
                        finish()
                    }
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "Email tidak valid atau tidak terdaftar", Toast.LENGTH_SHORT).show()
                        loading.visibility = View.GONE
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "Password salah", Toast.LENGTH_SHORT).show()
                        loading.visibility = View.GONE
                    } catch (e: Exception) {
                        Toast.makeText(this, "Masalah saat masuk: " + e.message, Toast.LENGTH_SHORT).show()
                        loading.visibility = View.GONE
                    }
                }
            }
    }


    private fun showErrorEmail(layout: TextInputLayout, text: CharSequence) {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

        if (!text.matches(emailPattern.toRegex())) {
            layout.error = "Email format is invalid"
            layout.isErrorEnabled = true
        } else {
            layout.isErrorEnabled = false
        }
    }


    private fun showErrorPassword(layout: TextInputLayout, text: CharSequence) {
        if (text.length in 1..7) {
            layout.error = "Password kurang dari 8 karakter"
            layout.isErrorEnabled = true
        } else {
            layout.isErrorEnabled = false
        }
    }

    private fun createTextWatcher(layout: TextInputLayout, showErrorFunc: (CharSequence) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showErrorFunc(s ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun isInputValid(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            inputEmailLayout.error = "Email is required"
            isValid = false
        } else {
            inputEmailLayout.isErrorEnabled = false
        }

        if (password.isEmpty()) {
            inputPasswordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            inputPasswordLayout.error = "Password must be at least 8 characters"
            isValid = false
        } else {
            inputPasswordLayout.isErrorEnabled = false
        }

        return isValid
    }
}
