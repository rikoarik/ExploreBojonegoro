package com.gracedian.explorebojonegoro.ui.splashscreen

import SharedPrefManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.DashboardActivity
import com.gracedian.explorebojonegoro.ui.onboarding.OnboardingActivity
import com.gracedian.explorebojonegoro.ui.welcome.WelcomeActivity

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private val SPLASH_DELAY: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        if (isInternetAvailable()) {
            simulateProgress()
        } else {
            Toast.makeText(this, "Periksa Kembali Koneksi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun simulateProgress() {
        Handler().postDelayed({
            startNextActivity()
        }, SPLASH_DELAY)

    }

    private fun startNextActivity() {
        val isFirstInstall = SharedPrefManager.isFirstInstall(this)
        val isLoggedIn = SharedPrefManager.isLoggedIn(this)

        val intent = if (isFirstInstall) {
            Intent(this, OnboardingActivity::class.java)
        }else if (isLoggedIn) {
            Intent(this, DashboardActivity::class.java)
        } else{
            Intent(this, WelcomeActivity::class.java)
        }

        startActivity(intent)
        finish()

    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
}
