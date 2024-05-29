package com.gracedian.explorebojonegoro.ui.dashboard.mytrips.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.mytrips.fragment.FragmentMytrips
import com.gracedian.explorebojonegoro.ui.dashboard.mytrips.fragment.FragmentWishlist

class MyTripsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentWishlist()
            1 -> FragmentMytrips()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

}
