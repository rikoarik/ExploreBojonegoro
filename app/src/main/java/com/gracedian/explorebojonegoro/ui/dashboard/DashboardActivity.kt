package com.gracedian.explorebojonegoro.ui.dashboard

import CustomPagerAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var adapter: CustomPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)

        bottomBar = findViewById(R.id.bottomBar)
        viewPager = findViewById(R.id.viewPager)

        // Initialize adapter
        adapter = CustomPagerAdapter(this)

        // Add fragments to the adapter
        adapter.addFragment(HomeFragment())
        adapter.addFragment(MapsFragment())
        adapter.addFragment(MyTripsFragment())
        adapter.addFragment(ProfileFragment())

        // Set adapter to ViewPager2
        viewPager.adapter = adapter

        // Set ViewPager2 page change listener
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomBar.setSelected(position, false)
            }
        })

        // Set bottom bar click listener
        bottomBar.addBubbleListener { id ->
            when (id) {
                R.id.nav_home -> viewPager.setCurrentItem(0, true)
                R.id.nav_maps -> viewPager.setCurrentItem(1, true)
                R.id.nav_my_trips -> viewPager.setCurrentItem(2, true)
                R.id.nav_profile -> viewPager.setCurrentItem(3, true)
            }
        }

        viewPager.isUserInputEnabled = false
    }


}


