package com.gracedian.explorebojonegoro.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.gracedian.explorebojonegoro.R

class RegisterActivity : AppCompatActivity() {

    private lateinit var inputName: TextInputEditText
    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputPassword: TextInputEditText
    private lateinit var inputConfirmPassword: TextInputEditText
    private lateinit var inputNameLayout: TextInputLayout
    private lateinit var inputEmailLayout: TextInputLayout
    private lateinit var inputPasswordLayout: TextInputLayout
    private lateinit var inputConfirmPasswordLayout: TextInputLayout
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var btLogin: TextView
    private lateinit var btBack: ImageView
    private lateinit var loadingBar: ProgressBar

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        inputNameLayout = findViewById(R.id.inputLayoutName)
        inputEmailLayout = findViewById(R.id.inputLayoutEmail)
        inputPasswordLayout = findViewById(R.id.inputLayoutPassword)
        inputConfirmPasswordLayout = findViewById(R.id.inputLayoutConfirmPassword)
        inputName = findViewById(R.id.inoutNama)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        btLogin = findViewById(R.id.btLogin)
        loadingBar = findViewById(R.id.loadingBar)
        btBack = findViewById(R.id.btBack)

        btLogin.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
        btBack.setOnClickListener {
            finish()
        }

        inputEmail.addTextChangedListener(createTextWatcher(inputEmailLayout) { text ->
            showErrorEmail(inputEmailLayout, text)
        })
        inputPassword.addTextChangedListener(createTextWatcher(inputPasswordLayout) { text ->
            showErrorPassword(inputPasswordLayout, text)
        })
        inputConfirmPassword.addTextChangedListener(createTextWatcher(inputConfirmPasswordLayout) { text ->
            showErrorPassword(inputConfirmPasswordLayout, text)
        })

        buttonRegister.setOnClickListener {
            val name = inputName.text.toString()
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            val confirmPassword = inputConfirmPassword.text.toString()
            buttonRegister.isClickable = false
            loadingBar.visibility = View.VISIBLE

            if (isInputValid(name, email, password, confirmPassword)) {
                registerUser(email, password, name)
                buttonRegister.isClickable = true
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

    private fun isInputValid(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            inputNameLayout.error = "Nama is required"
            isValid = false
        } else {
            inputNameLayout.isErrorEnabled = false
        }

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

        if (confirmPassword.isEmpty()) {
            inputConfirmPasswordLayout.error = "Confirm Password is required"
            isValid = false
        } else if (confirmPassword != password) {
            inputConfirmPasswordLayout.error = "Passwords do not match"
            isValid = false
        } else {
            inputConfirmPasswordLayout.isErrorEnabled = false
        }

        return isValid
    }
    private fun registerUser(email: String, password: String, name: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { signInMethods ->
                if (signInMethods.result?.signInMethods?.isNotEmpty() == true) {
                    inputEmailLayout.error = "Email is already registered"
                    loadingBar.visibility = View.GONE
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user != null) {
                                    val userId = user.uid
                                    val userRef = database.reference.child("users").child(userId)
                                    val userData = hashMapOf(
                                        "name" to name,
                                        "email" to email

                                    )
                                    userRef.setValue(userData)
                                    val intent = Intent(this, SignInActivity::class.java)
                                    startActivity(intent)
                                    loadingBar.visibility = View.GONE
                                    finish()

                                }
                            } else {
                                Log.e("Register", "Gagal")
                                loadingBar.visibility = View.GONE
                            }
                        }
                }
            }
    }

}