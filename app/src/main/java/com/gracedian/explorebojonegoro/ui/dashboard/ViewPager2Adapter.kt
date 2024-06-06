package com.gracedian.explorebojonegoro.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.HomeFragment
import com.gracedian.explorebojonegoro.ui.dashboard.maps.MapsFragment
import com.gracedian.explorebojonegoro.ui.dashboard.mytrips.MyTripsFragment
import com.gracedian.explorebojonegoro.ui.dashboard.profile.ProfileFragment

class ViewPager2Adapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> MapsFragment()
            2 -> MyTripsFragment()
            3 -> ProfileFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
    override fun getItemCount(): Int {
        return 4
    }
}