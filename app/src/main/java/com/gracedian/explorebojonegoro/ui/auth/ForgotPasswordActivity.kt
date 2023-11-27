package com.gracedian.explorebojonegoro.ui.auth

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.gracedian.explorebojonegoro.R


class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputEmailLayout: TextInputLayout
    private lateinit var sendEmailButton: AppCompatButton
    private lateinit var btBack: ImageView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        inputEmailLayout = findViewById(R.id.inputLayoutEmail)
        inputEmail = findViewById(R.id.inputEmail)
        sendEmailButton = findViewById(R.id.sendEmail)
        btBack = findViewById(R.id.btBack)
        auth = FirebaseAuth.getInstance()

        inputEmail.addTextChangedListener(createTextWatcher(inputEmailLayout) { text ->
            showErrorEmail(inputEmailLayout, text)
        })

        btBack.setOnClickListener {
            finish()
        }

        sendEmailButton.setOnClickListener {
            val email = inputEmail.text.toString()

            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            messageDialog()
                        } else {
                            Toast.makeText(this, "Gagal mengirim email pemulihan kata sandi.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Silakan isi alamat email Anda terlebih dahulu.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun messageDialog(){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("Email pemulihan kata sandi telah dikirim.\nSilahkan cek email Anda")
            .setTitle("Pesan Pemulihan Kata Sandi")
            .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                finish()
            })
        val dialog = alertDialog.create()
        dialog.show()

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
    private fun createTextWatcher(layout: TextInputLayout, showErrorFunc: (CharSequence) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showErrorFunc(s ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }
}