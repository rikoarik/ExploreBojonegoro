package com.gracedian.explorebojonegoro.ui.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.HomeFragment
import com.gracedian.explorebojonegoro.ui.dashboard.maps.MapsFragment
import com.gracedian.explorebojonegoro.ui.dashboard.mytrips.MyTripsFragment
import com.gracedian.explorebojonegoro.ui.dashboard.profile.ProfileFragment
import io.ak1.BubbleTabBar

class DashboardActivity : AppCompatActivity() {

    private lateinit var bottomBar: BubbleTabBar
    private lateinit var viewPager: ViewPager2
    private var selectedId: Int = 0
    private var doubleBackToExitPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bottomBar = findViewById(R.id.bottomBar)
        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ViewPager2Adapter(this)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomBar.setSelected(position)
            }
        })

        bottomBar.addBubbleListener { id ->
            selectedId = id
            when (id) {
                R.id.nav_home -> viewPager.currentItem = 0
                R.id.nav_maps -> viewPager.currentItem = 1
                R.id.nav_my_trips -> viewPager.currentItem = 2
                R.id.nav_profile -> viewPager.currentItem = 3
            }
        }
        viewPager.isUserInputEnabled = false

    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            finish()
            return
        }
        if (viewPager.currentItem >= 1) {
            viewPager.setCurrentItem(0, true)
        } else if (viewPager.currentItem == 0) {
            doubleBackToExitPressedOnce = true
            Toast.makeText(this@DashboardActivity, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }


}
