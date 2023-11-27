package com.gracedian.explorebojonegoro.ui.welcome

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.auth.RegisterActivity
import com.gracedian.explorebojonegoro.ui.auth.SignInActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var btLogin: AppCompatButton
    private lateinit var btRegister: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        SharedPrefManager.isFirstInstall(this)

        btLogin = findViewById(R.id.btLogin)
        btRegister = findViewById(R.id.btRegister)

        btLogin.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        btRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}