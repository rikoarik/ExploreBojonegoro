package com.gracedian.explorebojonegoro.ui.dashboard

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.gracedian.explorebojonegoro.R
import com.gracedian.explorebojonegoro.ui.dashboard.home.HomeFragment
import com.gracedian.explorebojonegoro.ui.dashboard.maps.MapsFragment
import com.gracedian.explorebojonegoro.ui.dashboard.mytrips.MyTripsFragment
import com.gracedian.explorebojonegoro.ui.dashboard.profile.ProfileFragment
import io.ak1.BubbleTabBar
import io.ak1.OnBubbleClickListener

class DashboardActivity : AppCompatActivity() {

    private lateinit var bottomBar: BubbleTabBar
    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        bottomBar = findViewById(R.id.bottomBar)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        val fragment1 = HomeFragment()
        val fragment2 = MapsFragment()
        val fragment3 = MyTripsFragment()
        val fragment4 = ProfileFragment()
        if (fragmentContainer.isEmpty()){
            replaceFragment(fragment1)
        }

        bottomBar.addBubbleListener { id ->
            when (id) {
                R.id.nav_home -> replaceFragment(fragment1)
                R.id.nav_maps -> replaceFragment(fragment2)
                R.id.nav_my_trips -> replaceFragment(fragment3)
                R.id.nav_profile -> replaceFragment(fragment4)
            }
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
