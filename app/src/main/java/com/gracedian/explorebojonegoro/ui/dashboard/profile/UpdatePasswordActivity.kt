package com.gracedian.explorebojonegoro.ui.dashboard.profile

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.gracedian.explorebojonegoro.R
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class UpdatePasswordActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    private lateinit var inputOldPassword: TextInputEditText
    private lateinit var inputNewPassword: TextInputEditText
    private lateinit var inputNewConfirmPassword: TextInputEditText
    private lateinit var inputLayoutOldPassword: TextInputLayout
    private lateinit var inputLayoutPassword: TextInputLayout
    private lateinit var inputLayoutConfirmPassword: TextInputLayout
    private lateinit var savePassword: Button
    private lateinit var btBack: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)

        inputOldPassword = findViewById(R.id.inputOldPassword)
        inputNewPassword = findViewById(R.id.inputNewPassword)
        inputNewConfirmPassword = findViewById(R.id.inputNewConfirmPassword)
        btBack = findViewById(R.id.btBack)
        savePassword = findViewById(R.id.savePassword)

        inputLayoutOldPassword = findViewById(R.id.inputLayoutOldPassword)
        inputLayoutPassword = findViewById(R.id.inputLayoutPassword)
        inputLayoutConfirmPassword = findViewById(R.id.inputLayoutConfirmPassword)

        btBack.setOnClickListener {
            finish()
        }

        savePassword.setOnClickListener {
            val oldPassword = inputOldPassword.text.toString()
            val newPassword = inputNewPassword.text.toString()
            val confirmPassword = inputNewConfirmPassword.text.toString()

            if (newPassword == confirmPassword) {
                if (isInputValid(newPassword)) {

                    val user = auth.currentUser
                    val credential = EmailAuthProvider.getCredential(user?.email ?: "", oldPassword)

                    user?.reauthenticate(credential)
                        ?.addOnCompleteListener { reauthResult ->
                            if (reauthResult.isSuccessful) {
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener { passwordUpdateResult ->
                                        if (passwordUpdateResult.isSuccessful) {
                                            Toast.makeText(
                                                this,
                                                "Password successfully updated",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Failed to update password",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Authentication failed. Incorrect old password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this, "New password and confirmation do not match", Toast.LENGTH_SHORT).show()
            }
        }
        inputOldPassword.addTextChangedListener(createTextWatcher(inputLayoutOldPassword) { showErrorPassword(it) })
        inputNewPassword.addTextChangedListener(createTextWatcher(inputLayoutPassword) { showErrorPassword(it) })
        inputNewConfirmPassword.addTextChangedListener(createTextWatcher(inputLayoutConfirmPassword) { showErrorPassword(it) })
    }

    private fun createTextWatcher(layout: TextInputLayout, showErrorFunc: (CharSequence) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                showErrorFunc(s ?: "")            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun showErrorPassword(text: CharSequence) {
        if (text.length in 1..7) {
            val errorMassage = "Password should be at least 8 characters"
            inputLayoutPassword.error = errorMassage
            inputLayoutOldPassword.error = errorMassage
            inputLayoutConfirmPassword.error = errorMassage
            inputLayoutOldPassword.isErrorEnabled = true
            inputLayoutConfirmPassword.isErrorEnabled = true
            inputLayoutPassword.isErrorEnabled = true
        } else {
            inputLayoutPassword.isErrorEnabled = false
            inputLayoutOldPassword.isErrorEnabled = false
            inputLayoutConfirmPassword.isErrorEnabled = false
            inputLayoutPassword.isErrorEnabled = false
        }
    }

    private fun isInputValid(password: String): Boolean {
        if (password.length < 8) {
            Toast.makeText(this, "Password should be at least 8 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}
