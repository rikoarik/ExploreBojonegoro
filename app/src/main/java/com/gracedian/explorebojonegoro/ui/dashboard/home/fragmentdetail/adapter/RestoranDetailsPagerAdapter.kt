package com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran.GalleryRestoranFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran.TentangRestoranFragment
import com.gracedian.explorebojonegoro.ui.dashboard.home.fragmentdetail.fragmentdetailsrestoran.UlasanRestoranFragment

class RestoranDetailsPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var data: Bundle? = null
    private var fragmentList: List<Fragment> = emptyList()

    fun setFragmentList(list: List<Fragment>) {
        fragmentList = list
    }

    fun setData(data: Bundle) {
        this.data = data
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val tentangFragment = TentangRestoranFragment()
                tentangFragment.arguments = data
                tentangFragment
            }
            1 -> {
                val galeriFragment = GalleryRestoranFragment()
                galeriFragment.arguments = data
                galeriFragment
            }

            2 -> {
                val ulasanFragment = UlasanRestoranFragment()
                ulasanFragment.arguments = data
                ulasanFragment
            }
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}
